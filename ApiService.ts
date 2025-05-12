import HmacSHA256 from "crypto-js/hmac-sha256"
import { v4 as uuidv4 } from "uuid"
import RequireRecaptchaVerificationError from "./Errors/RequireRecaptchaVerificationError"
import RequireSmsVerificationError from "./Errors/RequireSmsVerificationError"
import RequireIpQualityVerificationError from "./Errors/RequireIpQualityVerificationError"
import RequireEmailVerificationError from "./Errors/RequireEmailVerificationError"
import RequireFaceVerificationError from "./Errors/RequireFaceVerificationError"
import TeapotMessageError from "./Errors/TeapotMessageError"
import ForbiddenError from "./Errors/ForbiddenError"
import NotFoundError from "./Errors/NotFoundError"
import UnprocessableEntityError from "./Errors/UnprocessableEntityError"
import BadRequestError from "./Errors/BadRequestError"
import ServerError from "./Errors/ServerError"
import CustomApiError from "./Errors/CustomApiError"
import { debug } from "@hornet-web-react/core/utils"
import { type AppConfig } from "../AppConfig"
import { UNKNOWN_DEVICE_ID } from "@hornet-web-react/core/utils/constants"
import LoggerService from "../LoggerService"
import NetworkError from "./Errors/NetworkError"
import invariant from "tiny-invariant"
import { ApiServiceEndpointType } from "./ApiServiceEndpoint"
import {
  CurrentAppUrl,
  DeviceId,
  DeviceLocation,
} from "@hornet-web-react/core/types/session"
import LocalStorageService, { StorageKey } from "../LocalStorageService"

export type ApiRequest = {
  requestUrl: {
    url: string
    api?: ApiServiceEndpointType["api"]
    host?: string
    namespace?: string
    fallbacks?: string[]
  }
  params: RequestParams
  isRetry: boolean
  isFallback: boolean
}

export type ApiEndpointPayload = object | FormData | null

export type Headers = Record<string, string>

interface RequestParams {
  method: string
  headers: Headers
  body?: FormData | string
  signal?: AbortSignal
}

interface ApiError extends Error {
  status?: number
  originalPayload?: any
}

interface ReqSig {
  n: string
  s: string
  t: string
}

interface OriginalPayloadObject {
  t?: number
}

export interface ApiServiceContext {
  locale: string
  accessToken: string | null
  currentAppUrl: CurrentAppUrl
  deviceId: DeviceId
  profileId: string | null
  deviceLocation: DeviceLocation
  req2?: string | null
  req1?: string | null
  appName?: string
  timeZone?: string
}

export type ApiServiceFactory = (context: ApiServiceContext) => ApiService

class ApiService {
  protected readonly _appConfig: AppConfig
  protected readonly _loggerService: LoggerService
  protected readonly _localStorageService: LocalStorageService
  protected _context!: ApiServiceContext
  private _tsDiff: number
  private readonly _reqm: any
  protected readonly _fallbackDeviceId: string

  constructor(
    appConfig: AppConfig,
    loggerService: LoggerService,
    localStorageService: LocalStorageService,
    context: ApiServiceContext
  ) {
    debug(`${this.constructor.name}: constructor`)

    this._tsDiff = 0
    this._reqm = { n: "t", s: null, t: "s" }
    this._fallbackDeviceId = uuidv4()

    this._appConfig = appConfig
    this._loggerService = loggerService
    this._localStorageService = localStorageService
    this.updateContext(context)
  }

  updateContext(context: ApiServiceContext) {
    this._context = {
      ...context,
      deviceId:
        context.deviceId === UNKNOWN_DEVICE_ID
          ? this._fallbackDeviceId
          : context.deviceId,
    }
  }

  getLocale() {
    return this._context.locale
  }

  // HACK: for LoginService to determine whether user `isNew` or not
  getAppName() {
    return this._context.appName
  }

  useAbortableEndpoint<T>(
    endpoint: ApiServiceEndpointType,
    payload?: ApiEndpointPayload
  ): {
    abortController: AbortController
    apiRequest: () => Promise<T>
  } {
    debug(
      `${this.constructor.name}: useAbortableEndpoint ${
        endpoint.method
      } request to ${endpoint.url} with auth: ${
        endpoint.hasAuth ? "yes" : "no"
      }`
    )

    const controller = new AbortController()
    const signal = controller.signal

    const request = this.prepareRequestForEndpoint(endpoint, payload, signal)

    return {
      abortController: controller,
      apiRequest: () => this.makeRequest(request),
    }
  }

  async useEndpoint<T>(
    endpoint: ApiServiceEndpointType,
    payload?: ApiEndpointPayload
  ): Promise<T> {
    debug(
      `${this.constructor.name}: useEndpoint ${endpoint.method} request to ${
        endpoint.url
      } with auth: ${endpoint.hasAuth ? "yes" : "no"}`
    )

    const request = this.prepareRequestForEndpoint(endpoint, payload)

    return this.makeRequest(request)
  }

  static getEndpoint(
    endpoint: ApiServiceEndpointType,
    params: (string | number)[] = []
  ): ApiServiceEndpointType {
    // add params
    const placeholders = endpoint.url.match(/{[a-zA-Z]+}/g)
    if (placeholders && placeholders.length > 0) {
      invariant(
        placeholders.length === params.length,
        "Incorrect number of params for endpoint"
      )

      const url = placeholders.reduce(
        (acc: string, placeholder: string, index) => {
          return acc.replace(placeholder, String(params[index]))
        },
        endpoint.url
      )

      return {
        url,
        hasAuth: endpoint.hasAuth,
        method: endpoint.method,
        api: endpoint.api,
      }
    }

    // or return plain
    return endpoint
  }

  protected prepareRequestForEndpoint(
    endpoint: ApiServiceEndpointType,
    payload?: ApiEndpointPayload,
    signal?: AbortSignal
  ): ApiRequest {
    const requestUrl = this.buildUrlForEndpoint(endpoint)
    const headers = this.getHeaders(requestUrl.url)

    if (typeof payload !== "undefined" && this.isFormData(payload)) {
      delete headers["Content-Type"]
    }

    if (typeof headers["Authorization"] !== "undefined" && !endpoint.hasAuth) {
      delete headers["Authorization"]
    }

    const params: RequestParams = {
      method: endpoint.method,
      headers,
      signal,
    }

    if (typeof payload !== "undefined" && payload !== null) {
      params["body"] = this.isFormData(payload)
        ? (payload as FormData)
        : JSON.stringify(payload)
    }

    return {
      requestUrl,
      params,
      isRetry: false,
      isFallback: false,
    }
  }

  /**
   * @deprecated `useEndpoint` instead
   * @param url
   */
  async get(url: string) {
    const request = this.prepareRequest(url, "GET")

    return this.makeRequest(request)
  }

  /**
   * @deprecated `useEndpoint` instead
   * @param url
   * @param payload
   */
  async post(url: string, payload: ApiEndpointPayload) {
    const request = this.prepareRequest(url, "POST", payload)

    return this.makeRequest(request)
  }

  /**
   * @deprecated `useEndpoint` instead
   * @param url
   * @param payload
   */
  async put(url: string, payload: ApiEndpointPayload) {
    const request = this.prepareRequest(url, "PUT", payload)

    return this.makeRequest(request)
  }

  /**
   * @deprecated `useEndpoint` instead
   * @param url
   */
  async delete(url: string) {
    const request = this.prepareRequest(url, "DELETE")

    return this.makeRequest(request)
  }

  /**
   * @deprecated `useEndpoint` + `prepareRequestForEndpoint` instead
   * @param url
   * @param method
   * @param payload
   * @protected
   */
  protected prepareRequest(
    url: string,
    method: string,
    payload: ApiEndpointPayload = null
  ): ApiRequest {
    const requestUrl = this.buildUrl(url)
    const headers = this.getHeaders(requestUrl.url)

    if (this.isFormData(payload)) {
      delete headers["Content-Type"]
    }

    const params: RequestParams = {
      method: method,
      headers,
    }

    if (payload !== null) {
      params["body"] = this.isFormData(payload)
        ? (payload as FormData)
        : JSON.stringify(payload)
    }

    return {
      requestUrl,
      params,
      isRetry: false,
      isFallback: false,
    }
  }

  protected async makeRequest(request: ApiRequest, attempt = 1): Promise<any> {
    debug(
      `${this.constructor.name}: make ${request.params.method} request to ${request.requestUrl.url} with locale: ${this._context.locale}`
    )

    try {
      const response = await fetch(request.requestUrl.url, request.params)

      let data
      try {
        data = await response.json()
      } catch (e) {
        // nevermind
      }

      // save this for future requests
      if (request.isFallback && request.requestUrl.api) {
        const lsApiHost = this.getLocalStorageApiHost()

        if (lsApiHost[request.requestUrl.api] !== request.requestUrl.host) {
          this.updateLocalStorageApiHostFallback(
            request.requestUrl.api,
            request.requestUrl.host
          )
        }
      }

      return this.handleResponse(
        response.status,
        response.headers,
        data,
        request
      )
    } catch (error) {
      // maybe its AbortError and we don't need to worry about it
      if (
        typeof request.params.signal !== "undefined" &&
        request.params.signal.aborted
      ) {
        // just pass the error along without logging
        throw error
      }

      // network error or CORS issue, the only time when fetch rejects
      // https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API
      if (error instanceof Error) {
        // perhaps the host domain is blocked in the country, let's try some fallbacks if any
        if (
          typeof request.requestUrl.host !== "undefined" &&
          typeof request.requestUrl.fallbacks !== "undefined" &&
          request.requestUrl.fallbacks.length > 0
        ) {
          const fallbacks = [...request.requestUrl.fallbacks]

          // try this fallback, and remove it from future attempts
          const fallbackHost = fallbacks.shift()
          if (fallbackHost) {
            const fallbackRequest = {
              ...request,
              requestUrl: {
                fallbacks,
                host: fallbackHost,
                url: request.requestUrl.url.replace(
                  request.requestUrl.host,
                  fallbackHost
                ),
                api: request.requestUrl.api,
              },
              isFallback: true,
            }

            return this.makeRequest(fallbackRequest, attempt)
          }
        }

        // maybe it was a blip, and we can retry this in a few ms
        if (attempt <= 3) {
          await new Promise((resolve) => setTimeout(resolve, 500))
          return this.makeRequest(request, attempt + 1)
        }

        // one last attempt - maybe we're using fallbacks, and the fallback
        // is currently (or permanently) down, so we should drop that fallback
        // and just try the original host
        if (attempt === 4 && request.requestUrl.api) {
          const lsApiHost = this.getLocalStorageApiHost()

          if (lsApiHost[request.requestUrl.api] === request.requestUrl.host) {
            this.updateLocalStorageApiHostFallback(request.requestUrl.api, "")
          }

          // try one last time
          await new Promise((resolve) => setTimeout(resolve, 500))
          return this.makeRequest(request, attempt + 1)
        }

        // once we're happy with this retry-strategy, turn off this error logging
        const errorContext = this._loggerService.createLoggingContext({
          request_url: request.requestUrl.url,
          method: request.params.method,
        })

        this._loggerService.logExceptionWithSentry(error, errorContext)

        throw new NetworkError(error.message)
      }

      throw error
    }
  }

  private updateLocalStorageApiHostFallback(
    api: ApiServiceEndpointType["api"],
    host: string | undefined
  ) {
    this._localStorageService.setItem(StorageKey.api, {
      ...this._localStorageService.getItem(StorageKey.api),
      [api]: host || "",
    })
  }

  protected isFormData(payload: ApiEndpointPayload) {
    return typeof FormData !== "undefined" && payload instanceof FormData
  }

  protected createOriginalPayload(
    headers: Headers,
    payload: object | null = null
  ): OriginalPayloadObject {
    return { ...(payload ? payload : {}), t: Number(headers["X-S-T"]) || 0 }
  }

  /**
   * Error handling
   */
  protected async handleResponse(
    status: number,
    headers: HeadersInit,
    body: any,
    request: ApiRequest
  ) {
    if (444 === status && body.header && body.message) {
      throw this.decorateError(
        new RequireRecaptchaVerificationError({
          title: body.header,
          message: body.message,
        }),
        status,
        request
      )
    }

    if (446 === status && body.header && body.message) {
      throw this.decorateError(
        new RequireSmsVerificationError({
          title: body.header,
          message: body.message,
        }),
        status,
        request
      )
    }

    if (448 === status && body.header && body.message) {
      throw this.decorateError(
        new RequireIpQualityVerificationError({
          title: body.header,
          message: body.message,
        }),
        status,
        request
      )
    }

    if (449 === status && body.header && body.message) {
      throw this.decorateError(
        new RequireFaceVerificationError({
          title: body.header,
          message: body.message,
        }),
        status,
        request
      )
    }

    if (403 === status && body.header && body.message) {
      throw this.decorateError(
        new RequireEmailVerificationError({
          title: body.header,
          message: body.message,
        }),
        status,
        request
      )
    }

    // 461 = req signing fails due to invalid timestamp
    const requestDeltaT = Number(request.params.headers["X-S-T"] || 0)
    if (461 === status && body.t && requestDeltaT) {
      let tsDiff = body.t - requestDeltaT

      if (tsDiff !== 0) {
        // if there was previously set offset and this one is negative, it means
        // user fixed changed their time in the meantime, and we need to accommodate
        // for the difference
        const prevDiff = this._tsDiff
        if (prevDiff && tsDiff <= 0) {
          tsDiff = prevDiff + tsDiff
        }

        this._tsDiff = tsDiff
      }

      throw this.decorateError(
        new TeapotMessageError({
          title: body.header,
          message: body.message,
          t: body.t || 0,
        }),
        status,
        request
      )
    }

    if (401 === status || 403 === status) {
      throw this.decorateError(new ForbiddenError(), status, request)
    }

    if (404 === status) {
      throw this.decorateError(new NotFoundError(), status, request)
    }

    if (422 === status) {
      throw this.decorateError(
        new UnprocessableEntityError({
          errors: body?.errors || [],
          t: body?.t,
        }),
        status,
        request
      )
    }

    if (400 === status) {
      throw this.decorateError(new BadRequestError(), status, request)
    }

    if (500 === status) {
      throw this.decorateError(new ServerError(), status, request)
    }

    // 430 = account suspended
    // 430 = may only change your email twice every 12 hours
    if (
      [447, 418, 429, 445, 460, 430].includes(status) ||
      (body && body.header && body.message)
    ) {
      const title = body && body.header ? body.header : ""
      const message = body && body.message ? body.message : ""
      const t = body && body.t ? body.t : 0

      throw this.decorateError(
        new TeapotMessageError({
          title,
          message,
          t,
        }),
        status,
        request
      )
    }

    // generally not ok situation
    if (String(status)[0] !== "2") {
      throw this.decorateError(new CustomApiError(), status, request)
    }

    // HACK: some 204 have `undefined` result so let's return empty object instead
    return typeof body !== "undefined" ? body : {}
  }

  protected decorateError(
    error: ApiError,
    status: number,
    request: ApiRequest
  ) {
    const originalPayload = this.createOriginalPayload(
      request.params.headers,
      typeof request.params.body === "string"
        ? JSON.parse(request.params.body)
        : {}
    )

    error.status = status

    if (originalPayload) {
      error.originalPayload = originalPayload
    }

    return error
  }

  protected buildUrlForEndpoint(endpoint: ApiServiceEndpointType): {
    url: string
    api?: ApiServiceEndpointType["api"]
    host?: string
    namespace?: string
    fallbacks?: string[]
  } {
    if (endpoint.url.match(/^http/)) {
      return { url: endpoint.url }
    }

    // TODO: ApiService: cleanup these once we no longer use the direct `apiService.get|post|...` but
    // use the `useEndpoint` instead
    const finalUrl = endpoint.url.replace(/^(shop:\/|cmnty:\/)?\//, "")
    const { host, namespace, fallbacks } = {
      hornet: this._appConfig.hornet_api,
      shop: this._appConfig.hornet_shop_api,
      community: this._appConfig.community_api,
      quickies: this._appConfig.quickies_api,
    }[endpoint.api]

    // maybe we have override from localStorage
    const lsApiHost = this.getLocalStorageApiHost()

    if (lsApiHost[endpoint.api]) {
      return {
        url: `${lsApiHost[endpoint.api]}/${
          namespace ? namespace + "/" : ""
        }${finalUrl}`,
        host: lsApiHost[endpoint.api],
        namespace,
        fallbacks,
        api: endpoint.api,
      }
    }

    return {
      url: `${host}/${namespace ? namespace + "/" : ""}${finalUrl}`,
      host,
      namespace,
      fallbacks,
      api: endpoint.api,
    }
  }

  private getLocalStorageApiHost(): Record<
    ApiServiceEndpointType["api"],
    string
  > {
    return this._localStorageService.getItem(StorageKey.api)
  }

  /**
   * @deprecated `useEndpoint` + `buildUrlForEndpoint` instead
   * @param url
   * @protected
   */
  protected buildUrl(url: string): {
    url: string
    api?: ApiServiceEndpointType["api"]
    host?: string
    namespace?: string
    fallbacks?: string[]
  } {
    if (url.match(/^http/)) {
      return { url }
    }

    const finalUrl = url.replace(/^(shop:\/|cmnty:\/)?\//, "")
    const [host, namespace, fallbacks, api] = this.getHostname(url)

    // maybe we have override from localStorage
    const lsApiHost: Record<ApiServiceEndpointType["api"], string> =
      this._localStorageService.getItem(StorageKey.api)

    if (lsApiHost[api]) {
      return {
        url: `${lsApiHost[api]}/${namespace ? namespace + "/" : ""}${finalUrl}`,
        host: lsApiHost[api],
        namespace,
        fallbacks,
        api,
      }
    }

    return {
      url: `${host}/${namespace ? namespace + "/" : ""}${finalUrl}`,
      host,
      namespace,
      fallbacks,
      api,
    }
  }

  /**
   * @deprecated `useEndpoint` instead
   * @param appendix
   * @protected
   */
  protected getHostname(
    appendix: string
  ): [string, string, string[] | undefined, ApiServiceEndpointType["api"]] {
    if (this.isCommunityUrl(appendix)) {
      return [
        this._appConfig.community_api.host,
        this._appConfig.community_api.namespace,
        this._appConfig.community_api.fallbacks,
        "community",
      ]
    }

    if (this.isHornetShopUrl(appendix)) {
      return [
        this._appConfig.hornet_shop_api.host,
        this._appConfig.hornet_shop_api.namespace,
        this._appConfig.hornet_shop_api.fallbacks,
        "shop",
      ]
    }

    if (this.isHornetUrl(appendix)) {
      return [
        this._appConfig.hornet_api.host,
        this._appConfig.hornet_api.namespace,
        this._appConfig.hornet_api.fallbacks,
        "hornet",
      ]
    }

    // must be quickies then :grin:
    return [
      this._appConfig.quickies_api.host,
      this._appConfig.quickies_api.namespace,
      this._appConfig.quickies_api.fallbacks,
      "quickies",
    ]
  }

  protected isHornetUrl(url: string) {
    const hornetRoutes = [
      "session",
      "password",
      "sso",
      "lookup",
      "filters",
      "hornet_points_account",
      "honey_account",
      "accounts",
      "entitlements",
      "members",
      "favourites",
      "grids",
      "filters",
      "blocks",
      "reports",
    ]
    const hornetRoutesRegex = new RegExp(hornetRoutes.join("|"), "gi")

    return !!url.match(hornetRoutesRegex)
  }

  protected isHornetShopUrl(url: string) {
    return !!url.match(/^shop:\/\//)
  }

  protected isCommunityUrl(url: string) {
    return !!url.match(/^cmnty:\/\//)
  }

  protected getHeaders(url: string): Headers {
    const {
      locale,
      currentAppUrl,
      accessToken,
      deviceId,
      profileId,
      deviceLocation,
      req2,
      req1,
      appName,
      timeZone,
    } = this._context

    const headers: Headers = {
      Accept: "application/json",
      "Content-Type": "application/json",
      "X-Client-Version": "Web " + this._appConfig.version,
      "Accept-Language": locale,
      Referer: currentAppUrl,
    }

    if (accessToken) {
      headers["Authorization"] = `Hornet ${accessToken}`
    }

    if (deviceLocation) {
      headers["X-Device-Location"] = deviceLocation
    }

    if (appName) {
      headers["X-App"] = appName
    }

    if (timeZone) {
      headers["X-Timezone"] = timeZone
    }

    headers["X-Device-Identifier"] = deviceId

    const signature = this.getSignature(url)
    if (["internal", "external"].includes(signature)) {
      const { n, t, s } = this.addMetaToPayload(
        "n",
        {},
        signature === "external",
        deviceId,
        profileId,
        req2 || "",
        req1 || ""
      )
      headers["X-S-N"] = n
      headers["X-S-T"] = t
      headers["X-S-S"] = s
    }

    return headers
  }

  protected getSignature(url: string) {
    const internalRoutes = [
      "/profiles/verify$",
      "/reaction[s]?$",
      "/comments$",
      "/favourites$",
      "/activities/[0-9]+/award$",
      "sso_tokens$",
      "messages$",
      "quickies_members/near",
    ]
    const internalRoutesRegex = new RegExp(internalRoutes.join("|"), "gi")

    // req2
    if (url.match(internalRoutesRegex)) {
      return "internal"
    }

    // req1
    if (url.match(/\/accounts$|\/session$|\/sso_tokens\/exchange$/)) {
      return "external"
    }

    return ""
  }

  protected addMetaToPayload(
    k: string,
    p: any,
    sw: any,
    dId: any,
    pId: any,
    r2: string,
    r1: string
  ): ReqSig {
    sw = sw ? 1 : 0

    if (!p || p[k] || !k) {
      return p
    }

    p[k] =
      k === "n"
        ? uuidv4()
        : k === "t"
        ? Math.round(new Date().getTime() / 1000) + (this._tsDiff || 0)
        : HmacSHA256(
            sw
              ? [p["n"], dId, p["t"]].join(",")
              : [p["n"], dId, pId, p["t"]].join(","),
            sw ? process.env["REQ1"] || r1 || "" : r2 || ""
          ) + ""

    return this.addMetaToPayload(this._reqm[k], p, sw, dId, pId, r2, r1)
  }
}

export default ApiService
