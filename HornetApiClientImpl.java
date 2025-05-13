package com.hornet.android.net;

import android.content.Context;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import com.crashlytics.android.Crashlytics;
import com.facebook.share.internal.MessengerShareContentUtility;
import com.facebook.share.internal.ShareConstants;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.net.HttpHeaders;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.hornet.android.BuildConfig;
import com.hornet.android.Constants;
import com.hornet.android.HornetApplication;
import com.hornet.android.R;
import com.hornet.android.ads.AdScreens;
import com.hornet.android.analytics.EventParametersKt;
import com.hornet.android.chat.ChatsInteractor;
import com.hornet.android.features.awards.get_awards.GetAwardsResponse;
import com.hornet.android.features.awards.get_awards.GetAwardsTypesResponse;
import com.hornet.android.features.awards.give_award.GiveAwardRequest;
import com.hornet.android.features.onboarding.photo_select.SelectPhotoActivity;
import com.hornet.android.fragments.share.LocationViewFragment_;
import com.hornet.android.kernels.EntitlementKernel;
import com.hornet.android.kernels.FilterKernel;
import com.hornet.android.kernels.LookupKernel;
import com.hornet.android.kernels.MqttKernel;
import com.hornet.android.kernels.SessionKernel;
import com.hornet.android.models.net.BlockList;
import com.hornet.android.models.net.DeviceToken;
import com.hornet.android.models.net.FilterList;
import com.hornet.android.models.net.HashtagsListWrapper;
import com.hornet.android.models.net.MemberIdWrapper;
import com.hornet.android.models.net.PhotoPermissionList;
import com.hornet.android.models.net.PhotoWrapper;
import com.hornet.android.models.net.conversation.Channel;
import com.hornet.android.models.net.conversation.ConversationList;
import com.hornet.android.models.net.conversation.ConversationMessages;
import com.hornet.android.models.net.conversation.Message;
import com.hornet.android.models.net.conversation.MessageObjectWrapper;
import com.hornet.android.models.net.conversation.MessageResponse;
import com.hornet.android.models.net.conversation.OutgoingReadReceiptMessage;
import com.hornet.android.models.net.filters.Filter;
import com.hornet.android.models.net.lookup.LookupList;
import com.hornet.android.models.net.photo.FeedPhotoWrapper;
import com.hornet.android.models.net.photo.TempPhotoWrapper;
import com.hornet.android.models.net.product.currency.CurrencyAccount;
import com.hornet.android.models.net.product.currency.CurrencyAccountWrapper;
import com.hornet.android.models.net.product.currency.Honey;
import com.hornet.android.models.net.product.currency.HoneyShopProductsWrapper;
import com.hornet.android.models.net.product.currency.HoneyShopPurchaseWrapper;
import com.hornet.android.models.net.product.sticker.Sticker;
import com.hornet.android.models.net.product.subscription.Subscription;
import com.hornet.android.models.net.request.AddFavouriteRequest;
import com.hornet.android.models.net.request.BlockRequest;
import com.hornet.android.models.net.request.BranchTransactionRequest;
import com.hornet.android.models.net.request.CaptchaResponse;
import com.hornet.android.models.net.request.CommentWrapper;
import com.hornet.android.models.net.request.ContentLike;
import com.hornet.android.models.net.request.CreateAccountRequest;
import com.hornet.android.models.net.request.FeedbackRequest;
import com.hornet.android.models.net.request.HornetTransactionWrapper;
import com.hornet.android.models.net.request.MemberNoteWrapper;
import com.hornet.android.models.net.request.ReportRequest;
import com.hornet.android.models.net.request.SessionRequest;
import com.hornet.android.models.net.request.SmsVerifyRequest;
import com.hornet.android.models.net.request.SsoTokensRequest;
import com.hornet.android.models.net.request.TextCommentBody;
import com.hornet.android.models.net.request.UpdatePhotoIndexRequest;
import com.hornet.android.models.net.request.UpdatePhotoModeRequest;
import com.hornet.android.models.net.request.ViewedMeRequest;
import com.hornet.android.models.net.request.profile.AccountSetEmailOptedOutWrapper;
import com.hornet.android.models.net.request.profile.AccountSetEmailWrapper;
import com.hornet.android.models.net.request.profile.AccountSetPasswordWrapper;
import com.hornet.android.models.net.request.profile.AccountSetPhoneRequest;
import com.hornet.android.models.net.request.profile.AccountSetPublicWrapper;
import com.hornet.android.models.net.request.profile.AccountSetUsernameWrapper;
import com.hornet.android.models.net.request.profile.ProfileSelectiveUpdateWrapper;
import com.hornet.android.models.net.response.Activities;
import com.hornet.android.models.net.response.CampaignWrapper;
import com.hornet.android.models.net.response.Comment;
import com.hornet.android.models.net.response.CommentsWrapper;
import com.hornet.android.models.net.response.DiscoverResponse;
import com.hornet.android.models.net.response.Event;
import com.hornet.android.models.net.response.EventsWrapper;
import com.hornet.android.models.net.response.FavouriteResponse;
import com.hornet.android.models.net.response.FeedMomentUploadResult;
import com.hornet.android.models.net.response.FeedPhotoUploadResult;
import com.hornet.android.models.net.response.FullMemberWrapper;
import com.hornet.android.models.net.response.HornetBadgeProgressResponse;
import com.hornet.android.models.net.response.HornetPointsResponse;
import com.hornet.android.models.net.response.LedgerTransaction;
import com.hornet.android.models.net.response.LocationInfo;
import com.hornet.android.models.net.response.MemberList;
import com.hornet.android.models.net.response.OstWalletTransaction;
import com.hornet.android.models.net.response.PhotosList;
import com.hornet.android.models.net.response.Place;
import com.hornet.android.models.net.response.PlacesWrapper;
import com.hornet.android.models.net.response.ProfilePhotoUploadResult;
import com.hornet.android.models.net.response.SessionData;
import com.hornet.android.models.net.response.Stories;
import com.hornet.android.models.net.response.Story;
import com.hornet.android.models.net.response.Totals;
import com.hornet.android.models.net.response.WalletBalance;
import com.hornet.android.models.net.response.WalletDevice;
import com.hornet.android.models.net.response.WalletPassphrase;
import com.hornet.android.models.net.response.WalletSessionData;
import com.hornet.android.models.net.response.WalletSessionValues;
import com.hornet.android.models.net.response.WalletTransactionsList;
import com.hornet.android.models.net.stories.ActivityShare;
import com.hornet.android.models.net.stories.StoryShare;
import com.hornet.android.net.init_kernels.EmojiCompatKernel;
import com.hornet.android.product.ProductInteractor;
import com.hornet.android.utils.JsonUtils;
import com.hornet.android.utils.KeyUtil;
import com.hornet.android.utils.PhotoRequestBodyFactory;
import com.hornet.android.utils.PrefsDecorator;
import com.hornet.android.utils.helpers.KotlinHelpersKt;
import com.hornet.android.wallet.WalletInteractor;
import com.ost.walletsdk.OstSdk;
import com.ost.walletsdk.models.entities.OstDeviceManagerOperation;
import com.ost.walletsdk.models.entities.OstSessionKey;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import kotlin.Metadata;
import kotlin.Pair;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import org.eclipse.paho.android.service.MqttServiceConstants;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Metadata(bv = {1, 0, 3}, d1 = {"\u0000î\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u0000 \u00032\u00020\u0001:\u0004\u0003\u0003B\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0002\u0010\u0004J\b\u0010d\u001a\u00020eH\u0016J\u0010\u0010f\u001a\u00020g2\u0006\u0010h\u001a\u00020iH\u0016J\r\u0010j\u001a\u00020eH\u0000¢\u0006\u0002\bkJ\u0017\u0010l\u001a\u00020g2\b\u0010m\u001a\u0004\u0018\u00010nH\u0016¢\u0006\u0002\u0010oJ\u0018\u0010p\u001a\u00020g2\u0006\u0010m\u001a\u00020q2\u0006\u0010r\u001a\u00020\u0017H\u0016J\u0010\u0010s\u001a\u00020g2\u0006\u0010t\u001a\u00020qH\u0016J\u0010\u0010u\u001a\u00020g2\u0006\u0010v\u001a\u00020qH\u0016J\u001e\u0010w\u001a\b\u0012\u0004\u0012\u00020x0\f2\u0006\u0010m\u001a\u00020q2\u0006\u0010y\u001a\u00020zH\u0016J\u0010\u0010{\u001a\u00020g2\u0006\u0010|\u001a\u00020}H\u0016J\u0013\u0010~\u001a\u000202\t\u0010\u0001\u001a\u0004\u0018\u00010qH\u0016J\n\u0010\u0001\u001a\u00030\u0001H\u0002J\u0017\u0010\u0001\u001a\b\u0012\u0004\u0012\u00020x0\f2\u0006\u0010m\u001a\u00020qH\u0016J\t\u0010\u0001\u001a\u00020gH\u0016J\u001b\u0010\u0001\u001a\u00020g2\u0007\u0010\u0001\u001a\u00020q2\u0007\u0010\u0001\u001a\u00020nH\u0016J\u0012\u0010\u0001\u001a\u00020g2\u0007\u0010\u0001\u001a\u00020nH\u0016J\"\u0010\u0001\u001a\t\u0012\u0005\u0012\u00030\u00010\f2\u0007\u0010\u0001\u001a\u00020q2\u0007\u0010\u0001\u001a\u00020nH\u0016J\u0012\u0010\u0001\u001a\u00020g2\u0007\u0010\u0001\u001a\u00020qH\u0016J$\u0010\u0001\u001a\t\u0012\u0005\u0012\u00030\u00010\f2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J$\u0010\u0001\u001a\t\u0012\u0005\u0012\u00030\u00010\f2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J-\u0010\u0001\u001a\t\u0012\u0005\u0012\u00030\u00010\f2\u0007\u0010\u0001\u001a\u00020q2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J\u0012\u0010\u0001\u001a\u00020g2\u0007\u0010\u0001\u001a\u00020qH\u0016J\u0013\u0010\u0001\u001a\u00020g2\b\u0010\u0001\u001a\u00030\u0001H\u0016J+\u0010\u0001\u001a\t\u0012\u0005\u0012\u00030\u00010\f2\u0007\u0010\u0001\u001a\u00020q2\u0007\u0010\u0001\u001a\u00020n2\u0007\u0010\u0001\u001a\u00020qH\u0016J\t\u0010 \u0001\u001a\u00020gH\u0016J-\u0010¡\u0001\u001a\t\u0012\u0005\u0012\u00030¢\u00010\f2\u0007\u0010£\u0001\u001a\u00020:2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J\u0018\u0010¤\u0001\u001a\t\u0012\u0005\u0012\u00030¥\u00010\f2\u0006\u0010m\u001a\u00020nH\u0016J\u0019\u0010¦\u0001\u001a\t\u0012\u0005\u0012\u00030§\u00010\f2\u0007\u0010\u0001\u001a\u00020qH\u0016J.\u0010¨\u0001\u001a\t\u0012\u0005\u0012\u00030©\u00010\f2\u0007\u0010\u0001\u001a\u00020q2\t\u0010ª\u0001\u001a\u0004\u0018\u00010q2\b\u0010\u0001\u001a\u00030\u0001H\u0016J.\u0010«\u0001\u001a\t\u0012\u0005\u0012\u00030©\u00010\f2\u0007\u0010\u0001\u001a\u00020q2\t\u0010ª\u0001\u001a\u0004\u0018\u00010q2\b\u0010\u0001\u001a\u00030\u0001H\u0016J\u0019\u0010¬\u0001\u001a\t\u0012\u0005\u0012\u00030­\u00010\f2\u0007\u0010\u0001\u001a\u00020qH\u0016J\u0010\u0010®\u0001\u001a\t\u0012\u0005\u0012\u00030¯\u00010\fH\u0016J$\u0010°\u0001\u001a\t\u0012\u0005\u0012\u00030±\u00010\f2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J\u0018\u0010²\u0001\u001a\t\u0012\u0005\u0012\u00030³\u00010\f2\u0006\u0010m\u001a\u00020qH\u0016J.\u0010´\u0001\u001a\t\u0012\u0005\u0012\u00030µ\u00010\f2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u00012\b\u0010¶\u0001\u001a\u00030·\u0001H\u0016J.\u0010¸\u0001\u001a\t\u0012\u0005\u0012\u00030¹\u00010\f2\u0007\u0010º\u0001\u001a\u00020q2\t\u0010»\u0001\u001a\u0004\u0018\u00010q2\b\u0010\u0001\u001a\u00030\u0001H\u0016J\u0019\u0010¼\u0001\u001a\t\u0012\u0005\u0012\u00030½\u00010\f2\u0007\u0010¾\u0001\u001a\u00020qH\u0016J-\u0010¿\u0001\u001a\t\u0012\u0005\u0012\u00030¢\u00010\f2\u0007\u0010\u0001\u001a\u00020n2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J-\u0010À\u0001\u001a\t\u0012\u0005\u0012\u00030¢\u00010\f2\u0007\u0010\u0001\u001a\u00020n2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J-\u0010Á\u0001\u001a\t\u0012\u0005\u0012\u00030¢\u00010\f2\u0007\u0010\u0001\u001a\u00020n2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J$\u0010Â\u0001\u001a\t\u0012\u0005\u0012\u00030Ã\u00010\f2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J-\u0010Â\u0001\u001a\t\u0012\u0005\u0012\u00030Ã\u00010\f2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u00012\u0007\u0010\u0001\u001a\u00020nH\u0016J$\u0010Ä\u0001\u001a\t\u0012\u0005\u0012\u00030¢\u00010\f2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J5\u0010Å\u0001\u001a\t\u0012\u0005\u0012\u00030Æ\u00010\f2\b\u0010m\u001a\u0004\u0018\u00010n2\t\u0010Ç\u0001\u001a\u0004\u0018\u00010q2\b\u0010\u0001\u001a\u00030\u0001H\u0016¢\u0006\u0003\u0010È\u0001J\u0018\u0010É\u0001\u001a\t\u0012\u0005\u0012\u00030Ê\u00010\f2\u0006\u0010m\u001a\u00020nH\u0016J\u0019\u0010Ë\u0001\u001a\t\u0012\u0005\u0012\u00030Ê\u00010\f2\u0007\u0010Ì\u0001\u001a\u00020qH\u0016J\u0010\u0010Í\u0001\u001a\t\u0012\u0005\u0012\u00030Î\u00010\fH\u0016J\u0010\u0010Ï\u0001\u001a\t\u0012\u0005\u0012\u00030Î\u00010\fH\u0016J$\u0010Ð\u0001\u001a\t\u0012\u0005\u0012\u00030Ñ\u00010\f2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J\u0018\u0010Ò\u0001\u001a\b\u0012\u0004\u0012\u0002050\f2\u0007\u0010Ó\u0001\u001a\u00020qH\u0016J \u0010Ô\u0001\u001a\t\u0012\u0005\u0012\u00030Õ\u00010\f2\u000e\u0010Ö\u0001\u001a\t\u0012\u0004\u0012\u00020n0×\u0001H\u0016J\u0010\u0010Ø\u0001\u001a\t\u0012\u0005\u0012\u00030Ù\u00010\fH\u0016J\u0010\u0010Ú\u0001\u001a\t\u0012\u0005\u0012\u00030Û\u00010\fH\u0016J-\u0010Ü\u0001\u001a\t\u0012\u0005\u0012\u00030¢\u00010\f2\u0007\u0010\u0001\u001a\u00020n2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J-\u0010Ý\u0001\u001a\t\u0012\u0005\u0012\u00030¢\u00010\f2\u0007\u0010Þ\u0001\u001a\u00020q2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J,\u0010ß\u0001\u001a\t\u0012\u0005\u0012\u00030à\u00010\f2\u0006\u0010m\u001a\u00020n2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J,\u0010á\u0001\u001a\t\u0012\u0005\u0012\u00030à\u00010\f2\u0006\u0010m\u001a\u00020n2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J-\u0010â\u0001\u001a\t\u0012\u0005\u0012\u00030¹\u00010\f2\u0006\u0010m\u001a\u00020n2\t\u0010»\u0001\u001a\u0004\u0018\u00010q2\b\u0010\u0001\u001a\u00030\u0001H\u0016J-\u0010ã\u0001\u001a\t\u0012\u0005\u0012\u00030¹\u00010\f2\u0006\u0010m\u001a\u00020n2\t\u0010Ç\u0001\u001a\u0004\u0018\u00010q2\b\u0010\u0001\u001a\u00030\u0001H\u0016J$\u0010ä\u0001\u001a\t\u0012\u0005\u0012\u00030¢\u00010\f2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J%\u0010å\u0001\u001a\t\u0012\u0005\u0012\u00030¹\u00010\f2\t\u0010»\u0001\u001a\u0004\u0018\u00010q2\b\u0010\u0001\u001a\u00030\u0001H\u0016J$\u0010æ\u0001\u001a\t\u0012\u0005\u0012\u00030¢\u00010\f2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J$\u0010ç\u0001\u001a\t\u0012\u0005\u0012\u00030è\u00010\f2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J\u0019\u0010é\u0001\u001a\t\u0012\u0005\u0012\u00030ê\u00010\f2\u0007\u0010ë\u0001\u001a\u00020qH\u0016J$\u0010ì\u0001\u001a\t\u0012\u0005\u0012\u00030¢\u00010\f2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J5\u0010í\u0001\u001a\t\u0012\u0005\u0012\u00030¢\u00010\f2\u0007\u0010î\u0001\u001a\u00020q2\u0006\u0010m\u001a\u00020q2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J\"\u0010ï\u0001\u001a\t\u0012\u0005\u0012\u00030ð\u00010\f2\u0007\u0010ñ\u0001\u001a\u00020q2\u0007\u0010ª\u0001\u001a\u00020qH\u0016J\u0019\u0010ò\u0001\u001a\t\u0012\u0005\u0012\u00030ó\u00010\f2\u0007\u0010ô\u0001\u001a\u00020nH\u0016J\u0019\u0010õ\u0001\u001a\t\u0012\u0005\u0012\u00030ó\u00010\f2\u0007\u0010ö\u0001\u001a\u00020qH\u0016J$\u0010÷\u0001\u001a\t\u0012\u0005\u0012\u00030¢\u00010\f2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J%\u0010ø\u0001\u001a\t\u0012\u0005\u0012\u00030¹\u00010\f2\t\u0010»\u0001\u001a\u0004\u0018\u00010q2\b\u0010\u0001\u001a\u00030\u0001H\u0016J%\u0010ù\u0001\u001a\t\u0012\u0005\u0012\u00030¹\u00010\f2\t\u0010Ç\u0001\u001a\u0004\u0018\u00010q2\b\u0010\u0001\u001a\u00030\u0001H\u0016J$\u0010ú\u0001\u001a\t\u0012\u0005\u0012\u00030¢\u00010\f2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J\u001c\u0010û\u0001\u001a\u00020g2\u0007\u0010\u0001\u001a\u00020q2\b\u0010ü\u0001\u001a\u00030\u0001H\u0016J\u0013\u0010ý\u0001\u001a\u00020g2\b\u0010\u0001\u001a\u00030þ\u0001H\u0016J\t\u0010ÿ\u0001\u001a\u00020gH\u0016J\t\u0010\u0002\u001a\u00020eH\u0002J\u0012\u0010\u0002\u001a\u00020\u00172\u0007\u0010\u0001\u001a\u00020nH\u0002J%\u0010\u0002\u001a\u00020g2\u0007\u0010\u0001\u001a\u00020q2\u0007\u0010\u0001\u001a\u00020n2\b\u0010\u0002\u001a\u00030\u0002H\u0016J\"\u0010\u0002\u001a\t\u0012\u0005\u0012\u00030\u00020\f2\b\u0010\u0002\u001a\u00030\u00022\u0006\u0010\u0002\u001a\u00020\u0003H\u0016J\t\u0010\u0002\u001a\u00020gH\u0016J\u0011\u0010\u0002\u001a\u00020g2\u0006\u0010m\u001a\u00020nH\u0016J\u0013\u0010\u0002\u001a\u00020g2\b\u0010¶\u0001\u001a\u00030·\u0001H\u0016J\u0011\u0010\u0002\u001a\u00020e2\u0006\u0010\u0005\u001a\u00020\u0006H\u0016J\t\u0010\u0002\u001a\u00020eH\u0016J\t\u0010\u0002\u001a\u00020eH\u0016J\t\u0010\u0002\u001a\u00020eH\u0016J\"\u0010\u0002\u001a\t\u0012\u0005\u0012\u00030\u00010\f2\u0007\u0010\u0001\u001a\u00020q2\u0007\u0010\u0001\u001a\u00020qH\u0016J\u0019\u0010\u0002\u001a\t\u0012\u0005\u0012\u00030\u00020\f2\u0007\u0010\u0001\u001a\u00020nH\u0016J$\u0010\u0002\u001a\u00020g2\u0007\u0010î\u0001\u001a\u00020q2\u0006\u0010m\u001a\u00020q2\b\u0010\u0002\u001a\u00030\u0002H\u0016J\u001c\u0010\u0002\u001a\u00020g2\u0007\u0010\u0001\u001a\u00020q2\b\u0010\u0002\u001a\u00030\u0002H\u0016J\u001c\u0010\u0002\u001a\u00020g2\u0007\u0010¾\u0001\u001a\u00020q2\b\u0010\u0002\u001a\u00030\u0002H\u0016J\u001c\u0010\u0002\u001a\u00020g2\u0007\u0010ë\u0001\u001a\u00020q2\b\u0010\u0002\u001a\u00030\u0002H\u0016J\u001c\u0010\u0002\u001a\u00020g2\u0007\u0010ô\u0001\u001a\u00020n2\b\u0010\u0002\u001a\u00030\u0002H\u0016J\u0010\u0010\u0002\u001a\t\u0012\u0005\u0012\u00030\u00020\fH\u0016J\t\u0010\u0002\u001a\u00020gH\u0016J\u0019\u0010\u0002\u001a\u00020g2\t\u0010\u0001\u001a\u0004\u0018\u00010nH\u0016¢\u0006\u0002\u0010oJ\u0011\u0010\u0002\u001a\u00020g2\u0006\u0010m\u001a\u00020qH\u0016J\u0011\u0010\u0002\u001a\u00020g2\u0006\u0010m\u001a\u00020qH\u0016J\u001c\u0010\u0002\u001a\u00020g2\u0007\u0010\u0001\u001a\u00020q2\b\u0010 \u0002\u001a\u00030¡\u0002H\u0016J%\u0010¢\u0002\u001a\u00020g2\u0007\u0010\u0001\u001a\u00020q2\u0007\u0010\u0001\u001a\u00020n2\b\u0010 \u0002\u001a\u00030¡\u0002H\u0016J\u0013\u0010£\u0002\u001a\u00020g2\b\u0010¤\u0002\u001a\u00030¡\u0002H\u0016J\t\u0010¥\u0002\u001a\u00020gH\u0016J\u0012\u0010¦\u0002\u001a\u00020g2\u0007\u0010§\u0002\u001a\u00020qH\u0016J\t\u0010¨\u0002\u001a\u00020gH\u0016J-\u0010©\u0002\u001a\t\u0012\u0005\u0012\u00030¢\u00010\f2\u0007\u0010ª\u0002\u001a\u00020q2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J-\u0010«\u0002\u001a\t\u0012\u0005\u0012\u00030¢\u00010\f2\u0007\u0010Ì\u0001\u001a\u00020q2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016J\t\u0010¬\u0002\u001a\u00020gH\u0016J\u0013\u0010­\u0002\u001a\u00020g2\b\u0010®\u0002\u001a\u00030¯\u0002H\u0016J!\u0010°\u0002\u001a\u0010\u0012\f\u0012\n\u0012\u0005\u0012\u00030²\u00020±\u00020\f2\b\u0010ª\u0001\u001a\u00030³\u0002H\u0016J\u001e\u0010´\u0002\u001a\t\u0012\u0005\u0012\u00030\u00010\f2\f\u0010µ\u0002\u001a\u0007\u0012\u0002\b\u00030¶\u0002H\u0016J\u0013\u0010·\u0002\u001a\u00020g2\b\u0010µ\u0002\u001a\u00030¸\u0002H\u0016J\u0013\u0010¹\u0002\u001a\u00020g2\b\u0010º\u0002\u001a\u00030»\u0002H\u0016J\u0012\u0010¼\u0002\u001a\u00020g2\u0007\u0010§\u0002\u001a\u00020qH\u0016J\u0012\u0010½\u0002\u001a\u00020g2\u0007\u0010¾\u0002\u001a\u00020qH\u0016J\u0012\u0010¿\u0002\u001a\u00020g2\u0007\u0010À\u0002\u001a\u00020qH\u0016J\u0011\u0010Á\u0002\u001a\u00020g2\u0006\u0010r\u001a\u00020\u0017H\u0016J\u0012\u0010Â\u0002\u001a\u00020g2\u0007\u0010Ì\u0001\u001a\u00020qH\u0016J\u0012\u0010Ã\u0002\u001a\u00020g2\u0007\u0010Ä\u0002\u001a\u00020\u0017H\u0016J\u0013\u0010Å\u0002\u001a\u00020g2\b\u0010Æ\u0002\u001a\u00030Ç\u0002H\u0016J\u0013\u0010È\u0002\u001a\u00020g2\b\u0010#\u001a\u0004\u0018\u00010$H\u0016J\u001b\u0010É\u0002\u001a\u00020e2\u0007\u0010Ê\u0002\u001a\u00020q2\u0007\u0010Ë\u0002\u001a\u00020qH\u0002J\u001c\u0010Ì\u0002\u001a\u00020g2\u0007\u0010\u0001\u001a\u00020q2\b\u0010\u0001\u001a\u00030Í\u0002H\u0016J\u001c\u0010Î\u0002\u001a\u00020g2\u0007\u0010ô\u0001\u001a\u00020n2\b\u0010\u0001\u001a\u00030Ï\u0002H\u0016J\t\u0010Ð\u0002\u001a\u00020gH\u0016J\u0012\u0010Ñ\u0002\u001a\u00020g2\u0007\u0010Ò\u0002\u001a\u00020qH\u0016J\u0012\u0010Ó\u0002\u001a\u00020g2\u0007\u0010ô\u0001\u001a\u00020nH\u0016J\u0011\u0010Ô\u0002\u001a\u00020g2\u0006\u0010m\u001a\u00020nH\u0016J\u0013\u0010Õ\u0002\u001a\u00020e2\b\u0010£\u0001\u001a\u00030Ö\u0002H\u0016J\u0019\u0010×\u0002\u001a\u00020g2\u000e\u0010Ø\u0002\u001a\t\u0012\u0005\u0012\u00030Ù\u00020\rH\u0016J\u001a\u0010Ú\u0002\u001a\t\u0012\u0005\u0012\u00030Û\u00020\f2\b\u0010Ü\u0002\u001a\u00030Ý\u0002H\u0016JZ\u0010Þ\u0002\u001a\t\u0012\u0005\u0012\u00030ß\u00020\f2\n\u0010à\u0002\u001a\u0005\u0018\u00010á\u00022\n\u0010â\u0002\u001a\u0005\u0018\u00010ã\u00022\n\u0010ä\u0002\u001a\u0005\u0018\u00010\u00012\n\u0010å\u0002\u001a\u0005\u0018\u00010\u00012\t\u0010æ\u0002\u001a\u0004\u0018\u00010q2\u0007\u0010ç\u0002\u001a\u00020\u0017H\u0016¢\u0006\u0003\u0010è\u0002J8\u0010é\u0002\u001a\t\u0012\u0005\u0012\u00030ê\u00020\f2\b\u0010à\u0002\u001a\u00030á\u00022\b\u0010â\u0002\u001a\u00030ã\u00022\b\u0010ä\u0002\u001a\u00030\u00012\b\u0010å\u0002\u001a\u00030\u0001H\u0016J@\u0010ë\u0002\u001a/\u0012\u000b\u0012\t\u0012\u0005\u0012\u00030í\u00020\f\u0012\u001d\u0012\u001b\u0012\u0011\u0012\u000f\u0012\u0004\u0012\u00020n\u0012\u0004\u0012\u00020n0ì\u00020î\u0002j\u0003`ï\u00020ì\u00022\b\u0010à\u0002\u001a\u00030á\u0002H\u0016JJ\u0010ð\u0002\u001a\t\u0012\u0005\u0012\u00030ñ\u00020\f2\b\u0010à\u0002\u001a\u00030á\u00022\u0006\u0010r\u001a\u00020\u00172\b\u0010ò\u0002\u001a\u00030ã\u00022\b\u0010ó\u0002\u001a\u00030ã\u00022\b\u0010ä\u0002\u001a\u00030\u00012\b\u0010å\u0002\u001a\u00030\u0001H\u0016J\u0012\u0010ô\u0002\u001a\u00020g2\u0007\u0010ª\u0001\u001a\u00020qH\u0016J\u0010\u0010õ\u0002\u001a\t\u0012\u0005\u0012\u00030ö\u00020\fH\u0016J%\u0010÷\u0002\u001a\t\u0012\u0005\u0012\u00030ø\u00020\f2\t\u0010ù\u0002\u001a\u0004\u0018\u00010q2\b\u0010\u0001\u001a\u00030\u0001H\u0016J\u0010\u0010ú\u0002\u001a\t\u0012\u0005\u0012\u00030û\u00020\fH\u0016J\u0010\u0010ü\u0002\u001a\t\u0012\u0005\u0012\u00030ý\u00020\fH\u0016J\u001a\u0010þ\u0002\u001a\t\u0012\u0005\u0012\u00030\u00020\f2\b\u0010ÿ\u0002\u001a\u00030\u0002H\u0016J\u0018\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020x0\f2\u0007\u0010\u0003\u001a\u00020nH\u0016J/\u0010\u0003\u001a\t\u0012\u0005\u0012\u00030\u00030\f2\t\u0010\u0003\u001a\u0004\u0018\u00010q2\b\u0010\u0001\u001a\u00030\u00012\b\u0010\u0001\u001a\u00030\u0001H\u0016R\u001a\u0010\u0005\u001a\u00020\u0006X.¢\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR \u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\f8VX\u0004¢\u0006\u0006\u001a\u0004\b\u000f\u0010\u0010R\u001e\u0010\u0013\u001a\u00020\u00122\u0006\u0010\u0011\u001a\u00020\u0012@RX.¢\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u000e\u0010\u0002\u001a\u00020\u0003X\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u000e¢\u0006\u0002\n\u0000R\u001a\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00190\f8VX\u0004¢\u0006\u0006\u001a\u0004\b\u001a\u0010\u0010R\u001e\u0010\u001c\u001a\u00020\u001b2\u0006\u0010\u0011\u001a\u00020\u001b@RX.¢\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001eR\u001e\u0010 \u001a\u00020\u001f2\u0006\u0010\u0011\u001a\u00020\u001f@RX.¢\u0006\b\n\u0000\u001a\u0004\b!\u0010\"R\u001a\u0010#\u001a\b\u0012\u0004\u0012\u00020$0\f8VX\u0004¢\u0006\u0006\u001a\u0004\b%\u0010\u0010R\u000e\u0010&\u001a\u00020'X.¢\u0006\u0002\n\u0000R \u0010(\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020)0\r0\f8VX\u0004¢\u0006\u0006\u001a\u0004\b*\u0010\u0010R\u001a\u0010+\u001a\b\u0012\u0004\u0012\u00020,0\f8VX\u0004¢\u0006\u0006\u001a\u0004\b-\u0010\u0010R\u0011\u0010.\u001a\u00020/8F¢\u0006\u0006\u001a\u0004\b0\u00101R\u000e\u00102\u001a\u000203X.¢\u0006\u0002\n\u0000R\u001a\u00104\u001a\b\u0012\u0004\u0012\u0002050\f8VX\u0004¢\u0006\u0006\u001a\u0004\b6\u0010\u0010R\u0014\u00107\u001a\u00020\u00178VX\u0004¢\u0006\u0006\u001a\u0004\b7\u00108R\u0014\u00109\u001a\u00020\u00178VX\u0004¢\u0006\u0006\u001a\u0004\b9\u00108R\"\u0010;\u001a\u0004\u0018\u00010:2\b\u0010\u0011\u001a\u0004\u0018\u00010:@RX\u000e¢\u0006\b\n\u0000\u001a\u0004\b<\u0010=R\u001a\u0010>\u001a\b\u0012\u0004\u0012\u00020?0\f8VX\u0004¢\u0006\u0006\u001a\u0004\b@\u0010\u0010R\u001e\u0010B\u001a\u00020A2\u0006\u0010\u0011\u001a\u00020A@RX.¢\u0006\b\n\u0000\u001a\u0004\bC\u0010DR\u001a\u0010E\u001a\b\u0012\u0004\u0012\u00020F0\f8VX\u0004¢\u0006\u0006\u001a\u0004\bG\u0010\u0010R\u001e\u0010I\u001a\u00020H2\u0006\u0010\u0011\u001a\u00020H@RX.¢\u0006\b\n\u0000\u001a\u0004\bJ\u0010KR\u001a\u0010L\u001a\b\u0012\u0004\u0012\u0002050\f8VX\u0004¢\u0006\u0006\u001a\u0004\bM\u0010\u0010R\u000e\u0010N\u001a\u00020OX.¢\u0006\u0002\n\u0000R \u0010P\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\f8VX\u0004¢\u0006\u0006\u001a\u0004\bQ\u0010\u0010R\u001e\u0010S\u001a\u00020R2\u0006\u0010\u0011\u001a\u00020R@RX.¢\u0006\b\n\u0000\u001a\u0004\bT\u0010UR\u001e\u0010W\u001a\u00020V2\u0006\u0010\u0011\u001a\u00020V@RX.¢\u0006\b\n\u0000\u001a\u0004\bX\u0010YR \u0010Z\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020[0\r0\f8VX\u0004¢\u0006\u0006\u001a\u0004\b\\\u0010\u0010R\u001a\u0010]\u001a\b\u0012\u0004\u0012\u00020^0\f8VX\u0004¢\u0006\u0006\u001a\u0004\b_\u0010\u0010R\u001e\u0010a\u001a\u00020`2\u0006\u0010\u0011\u001a\u00020`@RX.¢\u0006\b\n\u0000\u001a\u0004\bb\u0010c¨\u0006\u0003"}, d2 = {"Lcom/hornet/android/net/HornetApiClientImpl;", "Lcom/hornet/android/net/HornetApiClient;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "application", "Lcom/hornet/android/HornetApplication;", "getApplication", "()Lcom/hornet/android/HornetApplication;", "setApplication", "(Lcom/hornet/android/HornetApplication;)V", "branchRedeemablePremiumMemberships", "Lio/reactivex/Single;", "Ljava/util/ArrayList;", "Lcom/hornet/android/models/net/product/subscription/Subscription;", "getBranchRedeemablePremiumMemberships", "()Lio/reactivex/Single;", "<set-?>", "Lcom/hornet/android/chat/ChatsInteractor;", "chatsInteractor", "getChatsInteractor", "()Lcom/hornet/android/chat/ChatsInteractor;", "didKernelInitRun", "", AdScreens.DISCOVER, "Lcom/hornet/android/models/net/response/DiscoverResponse;", "getDiscover", "Lcom/hornet/android/kernels/EntitlementKernel;", "entitlementKernel", "getEntitlementKernel", "()Lcom/hornet/android/kernels/EntitlementKernel;", "Lcom/hornet/android/kernels/FilterKernel;", "filterKernel", "getFilterKernel", "()Lcom/hornet/android/kernels/FilterKernel;", ShareConstants.WEB_DIALOG_PARAM_FILTERS, "Lcom/hornet/android/models/net/FilterList;", "getFilters", "firebaseAnalytics", "Lcom/google/firebase/analytics/FirebaseAnalytics;", "gemProducts", "Lcom/hornet/android/models/net/product/currency/Honey;", "getGemProducts", "getLocationInfo", "Lcom/hornet/android/models/net/response/LocationInfo;", "getGetLocationInfo", "gson", "Lcom/google/gson/Gson;", "getGson", "()Lcom/google/gson/Gson;", "hornetApiService", "Lcom/hornet/android/net/HornetApiService;", "interestsHashtags", "Lcom/hornet/android/models/net/HashtagsListWrapper;", "getInterestsHashtags", "isLoginNeededForSession", "()Z", "isSessionAvailable", "Lcom/google/android/gms/maps/model/LatLng;", LocationViewFragment_.LATLNG_ARG, "getLatlng", "()Lcom/google/android/gms/maps/model/LatLng;", "lookupData", "Lcom/hornet/android/models/net/lookup/LookupList;", "getLookupData", "Lcom/hornet/android/kernels/LookupKernel;", "lookupKernel", "getLookupKernel", "()Lcom/hornet/android/kernels/LookupKernel;", "messagesChannel", "Lcom/hornet/android/models/net/conversation/Channel;", "getMessagesChannel", "Lcom/hornet/android/kernels/MqttKernel;", "mqttKernel", "getMqttKernel", "()Lcom/hornet/android/kernels/MqttKernel;", "popularHashtags", "getPopularHashtags", "prefs", "Lcom/hornet/android/utils/PrefsDecorator;", "premiumMemberships", "getPremiumMemberships", "Lcom/hornet/android/product/ProductInteractor;", "productInteractor", "getProductInteractor", "()Lcom/hornet/android/product/ProductInteractor;", "Lcom/hornet/android/kernels/SessionKernel;", "sessionKernel", "getSessionKernel", "()Lcom/hornet/android/kernels/SessionKernel;", "stickers", "Lcom/hornet/android/models/net/product/sticker/Sticker;", "getStickers", "userTotals", "Lcom/hornet/android/models/net/response/Totals;", "getUserTotals", "Lcom/hornet/android/wallet/WalletInteractor;", "walletInteractor", "getWalletInteractor", "()Lcom/hornet/android/wallet/WalletInteractor;", "activatePremium", "", "addMemberNote", "Lio/reactivex/Completable;", "note", "Lcom/hornet/android/models/net/request/MemberNoteWrapper;", "afterInject", "afterInject$app_productionRelease", "blockUser", "id", "", "(Ljava/lang/Long;)Lio/reactivex/Completable;", "changePhotoMode", "", SelectPhotoActivity.IS_PUBLIC_PHOTO, "clearFiltersCategory", "category", "confirmFeedCampaign", "endpoint", "confirmTransaction", "Lcom/hornet/android/models/net/response/WalletTransactionsList$WalletTransactionWrapper;", "ostTransaction", "Lcom/hornet/android/models/net/response/OstWalletTransaction$OstWalletTransactionWrapper;", "createAccount", "createAccountRequest", "Lcom/hornet/android/models/net/request/CreateAccountRequest;", "createDeviceSignature", "", "special", "createSpecial", "Lcom/hornet/android/utils/KeyUtil$Special;", "declineTransaction", "deleteAccount", "deleteComment", "activityId", "commentId", "deleteConversation", "memberId", "deleteMessage", "Lcom/hornet/android/models/net/conversation/MessageResponse;", MqttServiceConstants.MESSAGE_ID, "deleteOwnActivity", "discoverEvents", "Lcom/hornet/android/models/net/response/EventsWrapper;", EventParametersKt.Page, "", "perPage", "discoverPlaces", "Lcom/hornet/android/models/net/response/PlacesWrapper;", "discoverStories", "Lcom/hornet/android/models/net/response/Stories$Wrapper;", "flavour", "doBranchRedeemPremiumMembershipTransaction", "productId", "doPremiumMembershipTransaction", "transactionWrapper", "Lcom/hornet/android/models/net/request/HornetTransactionWrapper;", "editComment", "Lcom/hornet/android/models/net/response/Comment$Wrapper;", "body", "endSession", AdScreens.EXPLORE, "Lcom/hornet/android/models/net/response/MemberList;", "location", "followMember", "Lcom/hornet/android/models/net/response/FavouriteResponse;", "getActivity", "Lcom/hornet/android/models/net/response/Activities$Activity$Wrapper;", "getActivityCommentsAfterToken", "Lcom/hornet/android/models/net/response/CommentsWrapper;", OstSdk.TOKEN, "getActivityCommentsBeforeToken", "getAwards", "Lcom/hornet/android/features/awards/get_awards/GetAwardsResponse;", "getAwardsTypes", "Lcom/hornet/android/features/awards/get_awards/GetAwardsTypesResponse;", "getBlockedUsers", "Lcom/hornet/android/models/net/BlockList;", "getCampaign", "Lcom/hornet/android/models/net/response/CampaignWrapper;", "getConversations", "Lcom/hornet/android/models/net/conversation/ConversationList;", AdScreens.INBOX, "Lcom/hornet/android/chat/ChatsInteractor$Companion$Inbox;", "getDynamicFeed", "Lcom/hornet/android/models/net/response/Activities$Wrapper;", "path", "afterToken", "getEventById", "Lcom/hornet/android/models/net/response/Event$Wrapper;", "eventId", "getFans", "getFavourites", "getFavouritesFan", "getFeedPhotos", "Lcom/hornet/android/models/net/photo/FeedPhotoWrapper$FeedPhotoListWrapper;", "getFollowCandidates", "getFullConversation", "Lcom/hornet/android/models/net/conversation/ConversationMessages;", "beforeToken", "(Ljava/lang/Long;Ljava/lang/String;I)Lio/reactivex/Single;", "getFullMember", "Lcom/hornet/android/models/net/response/FullMemberWrapper;", "getFullMemberByUsername", "username", "getGemAccount", "Lcom/hornet/android/models/net/product/currency/CurrencyAccount;", "getGemBalance", "getGemTransactions", "Lcom/hornet/android/models/net/product/currency/CurrencyAccountWrapper;", "getHashtagSuggestions", "query", "getHoneyShopProducts", "Lcom/hornet/android/models/net/product/currency/HoneyShopProductsWrapper;", "productIds", "", "getHornetBadgeProgress", "Lcom/hornet/android/models/net/response/HornetBadgeProgressResponse;", "getHornetPoints", "Lcom/hornet/android/models/net/response/HornetPointsResponse;", "getMatches", "getMemberForDynamicGrid", "grid", "getMemberPhotoStream", "Lcom/hornet/android/models/net/response/PhotosList;", "getMemberPrivatePhotos", "getMemberTimelineFeedAfterToken", "getMemberTimelineFeedBeforeToken", "getNearby", "getNotificationsAfterToken", "getOwnFavourites", "getPhotoPermissions", "Lcom/hornet/android/models/net/PhotoPermissionList;", "getPlaceById", "Lcom/hornet/android/models/net/response/Place$Wrapper;", "placeId", "getRecent", "getSpecificListMembers", OstDeviceManagerOperation.KIND, "getSsoTokens", "Lcom/hornet/android/net/SsoTokensResponse;", "destination", "getStoryById", "Lcom/hornet/android/models/net/response/Story$Wrapper;", "storyId", "getStoryBySlug", "storySlug", "getSuggested", "getTimelineFeedAfterToken", "getTimelineFeedBeforeToken", "getViewedMe", "giveAwards", "awardTypeId", "ignoreActivityMember", "Lcom/hornet/android/models/net/MemberIdWrapper;", "initKernels", "initRestAdapter", "isOwnProfile", "likeComment", "liked", "Lcom/hornet/android/models/net/request/ContentLike;", "login", "Lcom/hornet/android/models/net/response/SessionData;", "sessionRequest", "Lcom/hornet/android/models/net/request/SessionRequest;", "markAllAsRead", "markAsRead", "markInboxAsSeen", "onApplicationCreated", "onCreateKernels", "onResumeKernels", "onStartKernels", "postComment", "purchaseHoneyShopProduct", "Lcom/hornet/android/models/net/product/currency/HoneyShopPurchaseWrapper;", "reactTo", "reaction", "reactToActivity", "reactToEvent", "reactToPlace", "reactToStory", "recoverDevice", "Lcom/hornet/android/models/net/response/WalletDevice$WalletDeviceWrapper;", "removeAllBlocks", "removeBlock", "removePhoto", "removeUserWaitingForResponse", "reportActivity", "report", "Lcom/hornet/android/models/net/request/ReportRequest;", "reportComment", "reportUser", ShareConstants.WEB_DIALOG_RESULT_PARAM_REQUEST_ID, "resendEmailVerification", "resetPassword", "email", "revokeAllPhotoPermissions", "searchHashtags", "hashtags", "searchUsernames", "sendAttestation", "sendFeedback", "feedbackRequest", "Lcom/hornet/android/models/net/request/FeedbackRequest;", "sendGCMToken", "Lretrofit2/Response;", "Ljava/lang/Void;", "Lcom/hornet/android/models/net/DeviceToken;", "sendMessage", "message", "Lcom/hornet/android/models/net/conversation/Message;", "sendReadReceiptMessage", "Lcom/hornet/android/models/net/conversation/OutgoingReadReceiptMessage;", "sendViewedProfiles", "viewed", "Lcom/hornet/android/models/net/request/ViewedMeRequest;", "setAccountEmail", "setAccountPassword", "password", "setAccountPhone", "phone", "setAccountPublic", "setAccountUsername", "setEmailOptedOut", "isEmailOptedOut", "setFilter", "filter", "Lcom/hornet/android/models/net/filters/Filter;", "setFilters", "setUserProperty", OstSessionKey.KEY, "value", "shareActivity", "Lcom/hornet/android/models/net/stories/ActivityShare;", "shareStory", "Lcom/hornet/android/models/net/stories/StoryShare;", "smsVerificationResend", "smsVerificationVerify", "code", "trackStoryView", "unfollowMember", "updateLocation", "Landroid/location/Location;", "updatePhotoSlots", "photoWrappers", "Lcom/hornet/android/models/net/PhotoWrapper;", "updateProfile", "Lcom/hornet/android/models/net/response/SessionData$Session;", "profileUpdateWrapper", "Lcom/hornet/android/models/net/request/profile/ProfileSelectiveUpdateWrapper;", "uploadFeedMoment", "Lcom/hornet/android/models/net/response/FeedMomentUploadResult;", "file", "Ljava/io/File;", MessengerShareContentUtility.IMAGE_RATIO_SQUARE, "Landroid/graphics/Rect;", "originalWidth", SelectPhotoActivity.ORIGINAL_HEIGHT, ShareConstants.FEED_CAPTION_PARAM, "publicShare", "(Ljava/io/File;Landroid/graphics/Rect;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Z)Lio/reactivex/Single;", "uploadFeedPhoto", "Lcom/hornet/android/models/net/response/FeedPhotoUploadResult;", "uploadPhoto", "Lkotlin/Pair;", "Lcom/hornet/android/models/net/photo/TempPhotoWrapper;", "Lio/reactivex/Flowable;", "Lcom/hornet/android/net/UploadProgress;", "uploadProfilePhoto", "Lcom/hornet/android/models/net/response/ProfilePhotoUploadResult;", "profile", "avatar", "verifyCaptcha", "walletBalance", "Lcom/hornet/android/models/net/response/WalletBalance$WalletBalanceWrapper;", "walletLedgerTransaction", "Lcom/hornet/android/models/net/response/LedgerTransaction$LedgerWrapper;", "before", "walletLogin", "Lcom/hornet/android/models/net/response/WalletSessionData$WalletSessionWrapper;", "walletPassphrase", "Lcom/hornet/android/models/net/response/WalletPassphrase$WalletPassphraseWrapper;", "walletRegisterDevice", "deviceWrapper", "walletTransaction", "transactionId", "walletTransactions", "Lcom/hornet/android/models/net/response/WalletTransactionsList;", "status", "APIHeaderInterceptor", "Companion", "app_productionRelease"}, k = 1, mv = {1, 1, 13})
/* compiled from: HornetApiClientImpl.kt */
public final class HornetApiClientImpl implements HornetApiClient {
    public static final Companion Companion = new Companion(null);
    private static final String TAG = "HornetApp";
    private static HornetApiClientImpl instance;
    public HornetApplication application;
    private ChatsInteractor chatsInteractor;
    private final Context context;
    private boolean didKernelInitRun;
    private EntitlementKernel entitlementKernel;
    private FilterKernel filterKernel;
    private FirebaseAnalytics firebaseAnalytics;
    private HornetApiService hornetApiService;
    private volatile LatLng latlng;
    private LookupKernel lookupKernel;
    private MqttKernel mqttKernel;
    private PrefsDecorator prefs;
    private ProductInteractor productInteractor;
    private SessionKernel sessionKernel;
    private WalletInteractor walletInteractor;

    private HornetApiClientImpl(Context context2) {
        Context applicationContext = context2.getApplicationContext();
        Intrinsics.checkExpressionValueIsNotNull(applicationContext, "context.applicationContext");
        this.context = applicationContext;
    }

    public /* synthetic */ HornetApiClientImpl(Context context2, DefaultConstructorMarker defaultConstructorMarker) {
        this(context2);
    }

    public static final /* synthetic */ FirebaseAnalytics access$getFirebaseAnalytics$p(HornetApiClientImpl hornetApiClientImpl) {
        FirebaseAnalytics firebaseAnalytics2 = hornetApiClientImpl.firebaseAnalytics;
        if (firebaseAnalytics2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("firebaseAnalytics");
        }
        return firebaseAnalytics2;
    }

    public static final /* synthetic */ HornetApiService access$getHornetApiService$p(HornetApiClientImpl hornetApiClientImpl) {
        HornetApiService hornetApiService2 = hornetApiClientImpl.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        return hornetApiService2;
    }

    public static final /* synthetic */ PrefsDecorator access$getPrefs$p(HornetApiClientImpl hornetApiClientImpl) {
        PrefsDecorator prefsDecorator = hornetApiClientImpl.prefs;
        if (prefsDecorator == null) {
            Intrinsics.throwUninitializedPropertyAccessException("prefs");
        }
        return prefsDecorator;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public boolean isSessionAvailable() {
        return (getSessionKernel().getSession() != null) && this.didKernelInitRun;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public boolean isLoginNeededForSession() {
        return getSessionKernel().getSession() == null;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public HornetApplication getApplication() {
        HornetApplication hornetApplication = this.application;
        if (hornetApplication == null) {
            Intrinsics.throwUninitializedPropertyAccessException("application");
        }
        return hornetApplication;
    }

    public void setApplication(HornetApplication hornetApplication) {
        Intrinsics.checkParameterIsNotNull(hornetApplication, "<set-?>");
        this.application = hornetApplication;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public FilterKernel getFilterKernel() {
        FilterKernel filterKernel2 = this.filterKernel;
        if (filterKernel2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("filterKernel");
        }
        return filterKernel2;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public LookupKernel getLookupKernel() {
        LookupKernel lookupKernel2 = this.lookupKernel;
        if (lookupKernel2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("lookupKernel");
        }
        return lookupKernel2;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public MqttKernel getMqttKernel() {
        MqttKernel mqttKernel2 = this.mqttKernel;
        if (mqttKernel2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("mqttKernel");
        }
        return mqttKernel2;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public EntitlementKernel getEntitlementKernel() {
        EntitlementKernel entitlementKernel2 = this.entitlementKernel;
        if (entitlementKernel2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("entitlementKernel");
        }
        return entitlementKernel2;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public ChatsInteractor getChatsInteractor() {
        ChatsInteractor chatsInteractor2 = this.chatsInteractor;
        if (chatsInteractor2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("chatsInteractor");
        }
        return chatsInteractor2;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public WalletInteractor getWalletInteractor() {
        WalletInteractor walletInteractor2 = this.walletInteractor;
        if (walletInteractor2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("walletInteractor");
        }
        return walletInteractor2;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public ProductInteractor getProductInteractor() {
        ProductInteractor productInteractor2 = this.productInteractor;
        if (productInteractor2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("productInteractor");
        }
        return productInteractor2;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public SessionKernel getSessionKernel() {
        SessionKernel sessionKernel2 = this.sessionKernel;
        if (sessionKernel2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("sessionKernel");
        }
        return sessionKernel2;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public LatLng getLatlng() {
        return this.latlng;
    }

    public final void afterInject$app_productionRelease() {
        this.prefs = new PrefsDecorator(this.context);
        initRestAdapter();
    }

    @Override // com.hornet.android.net.HornetApiClient
    public void onApplicationCreated(HornetApplication hornetApplication) {
        Intrinsics.checkParameterIsNotNull(hornetApplication, "application");
        setApplication(hornetApplication);
        FirebaseAnalytics instance2 = FirebaseAnalytics.getInstance(this.context);
        Intrinsics.checkExpressionValueIsNotNull(instance2, "FirebaseAnalytics.getInstance(context)");
        this.firebaseAnalytics = instance2;
    }

    private final void initRestAdapter() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(HornetApiClientImpl$initRestAdapter$crashlyticsLoggingInterceptor$1.INSTANCE);
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        Object create = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(getGson())).addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync()).client(new OkHttpClient.Builder().connectTimeout(12, TimeUnit.SECONDS).readTimeout(12, TimeUnit.SECONDS).writeTimeout(12, TimeUnit.SECONDS).addInterceptor(new APIHeaderInterceptor()).addInterceptor(httpLoggingInterceptor).build()).baseUrl(Constants.BASE_URL).build().create(HornetApiService.class);
        Intrinsics.checkExpressionValueIsNotNull(create, "retrofit.create(HornetApiService::class.java)");
        this.hornetApiService = (HornetApiService) create;
    }

    public final Gson getGson() {
        return JsonUtils.INSTANCE.getFullFeaturedGsonInstance();
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<SessionData> login(SessionRequest sessionRequest, Context context2) {
        Intrinsics.checkParameterIsNotNull(sessionRequest, "sessionRequest");
        Intrinsics.checkParameterIsNotNull(context2, "context");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<SessionData> doOnSuccess = hornetApiService2.login(sessionRequest).doOnSuccess(new HornetApiClientImpl$login$1(this, sessionRequest, context2));
        Intrinsics.checkExpressionValueIsNotNull(doOnSuccess, "hornetApiService.login(s…eption(e)\n\t\t\t\t}\n\t\t\t}\n\t\t})");
        return doOnSuccess;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable createAccount(CreateAccountRequest createAccountRequest) {
        Intrinsics.checkParameterIsNotNull(createAccountRequest, "createAccountRequest");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable createAccount = hornetApiService2.createAccount(createAccountRequest);
        Intrinsics.checkExpressionValueIsNotNull(createAccount, "hornetApiService.createA…unt(createAccountRequest)");
        return createAccount;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable sendAttestation() {
        Completable create = Completable.create(new HornetApiClientImpl$sendAttestation$1(this));
        Intrinsics.checkExpressionValueIsNotNull(create, "Completable.create { emi….onError(error)\n\t\t\t})\n\t\t}");
        return create;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<Response<Void>> sendGCMToken(DeviceToken deviceToken) {
        Intrinsics.checkParameterIsNotNull(deviceToken, OstSdk.TOKEN);
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<Response<Void>> sendGCMToken = hornetApiService2.sendGCMToken(deviceToken);
        Intrinsics.checkExpressionValueIsNotNull(sendGCMToken, "hornetApiService.sendGCMToken(token)");
        return sendGCMToken;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<MemberList> explore(LatLng latLng, int i, int i2) {
        Intrinsics.checkParameterIsNotNull(latLng, "location");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<MemberList> explore = hornetApiService2.explore(latLng.latitude, latLng.longitude, i, i2);
        Intrinsics.checkExpressionValueIsNotNull(explore, "hornetApiService.explore…longitude, page, perPage)");
        return explore;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<MemberList> getRecent(int i, int i2) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<MemberList> recent = hornetApiService2.getRecent(i, i2);
        Intrinsics.checkExpressionValueIsNotNull(recent, "hornetApiService.getRecent(page, perPage)");
        return recent;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<MemberList> getSuggested(int i, int i2) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<MemberList> suggested = hornetApiService2.getSuggested(i, i2);
        Intrinsics.checkExpressionValueIsNotNull(suggested, "hornetApiService.getSuggested(page, perPage)");
        return suggested;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<MemberList> getMatches(long j, int i, int i2) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<MemberList> matches = hornetApiService2.getMatches(String.valueOf(j), i, i2);
        Intrinsics.checkExpressionValueIsNotNull(matches, "hornetApiService.getMatc…oString(), page, perPage)");
        return matches;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<MemberList> getFans(long j, int i, int i2) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<MemberList> fans = hornetApiService2.getFans(String.valueOf(j), i, i2);
        Intrinsics.checkExpressionValueIsNotNull(fans, "hornetApiService.getFans…oString(), page, perPage)");
        return fans;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<MemberList> getFavourites(long j, int i, int i2) {
        SessionData.Session session;
        FullMemberWrapper.FullMember profile;
        String valueOf = String.valueOf(j);
        SessionKernel sessionKernel2 = getSessionKernel();
        if (Intrinsics.areEqual(valueOf, String.valueOf((sessionKernel2 == null || (session = sessionKernel2.getSession()) == null || (profile = session.getProfile()) == null) ? null : profile.id))) {
            return getOwnFavourites(i, i2);
        }
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<MemberList> favourites = hornetApiService2.getFavourites(String.valueOf(j), i, i2);
        Intrinsics.checkExpressionValueIsNotNull(favourites, "hornetApiService.getFavo…oString(), page, perPage)");
        return favourites;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<MemberList> getOwnFavourites(int i, int i2) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<MemberList> ownFavourites = hornetApiService2.getOwnFavourites(i, i2);
        Intrinsics.checkExpressionValueIsNotNull(ownFavourites, "hornetApiService.getOwnFavourites(page, perPage)");
        return ownFavourites;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<MemberList> getFavouritesFan(long j, int i, int i2) {
        if (isOwnProfile(j)) {
            HornetApiService hornetApiService2 = this.hornetApiService;
            if (hornetApiService2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
            }
            Single<MemberList> favouritesFan = hornetApiService2.getFavouritesFan(i, i2);
            Intrinsics.checkExpressionValueIsNotNull(favouritesFan, "hornetApiService.getFavouritesFan(page, perPage)");
            return favouritesFan;
        }
        HornetApiService hornetApiService3 = this.hornetApiService;
        if (hornetApiService3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<MemberList> fans = hornetApiService3.getFans(String.valueOf(j), i, i2);
        Intrinsics.checkExpressionValueIsNotNull(fans, "hornetApiService.getFans…oString(), page, perPage)");
        return fans;
    }

    private final boolean isOwnProfile(long j) {
        FullMemberWrapper.FullMember profile;
        SessionData.Session session = getSessionKernel().getSession();
        return (session == null || (profile = session.getProfile()) == null || j != profile.id.longValue()) ? false : true;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<FullMemberWrapper> getFullMember(long j) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<FullMemberWrapper> fullMember = hornetApiService2.getFullMember(KotlinHelpersKt.toUnsignedString$default(j, 0, 1, null), this.context.getResources().getInteger(R.integer.gallery_preview_photos));
        Intrinsics.checkExpressionValueIsNotNull(fullMember, "hornetApiService.getFull….gallery_preview_photos))");
        return fullMember;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<FullMemberWrapper> getFullMemberByUsername(String str) {
        Intrinsics.checkParameterIsNotNull(str, "username");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<FullMemberWrapper> fullMemberByUsername = hornetApiService2.getFullMemberByUsername(str);
        Intrinsics.checkExpressionValueIsNotNull(fullMemberByUsername, "hornetApiService.getFullMemberByUsername(username)");
        return fullMemberByUsername;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<SessionData.Session> updateProfile(ProfileSelectiveUpdateWrapper profileSelectiveUpdateWrapper) {
        Intrinsics.checkParameterIsNotNull(profileSelectiveUpdateWrapper, "profileUpdateWrapper");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<SessionData.Session> updateProfile = hornetApiService2.updateProfile(profileSelectiveUpdateWrapper);
        Intrinsics.checkExpressionValueIsNotNull(updateProfile, "hornetApiService.updateP…ile(profileUpdateWrapper)");
        return updateProfile;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable setAccountPublic(boolean z) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable accountPublic = hornetApiService2.setAccountPublic(new AccountSetPublicWrapper(z));
        Intrinsics.checkExpressionValueIsNotNull(accountPublic, "hornetApiService.setAcco…tPublicWrapper(isPublic))");
        return accountPublic;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable setEmailOptedOut(boolean z) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable emailOptedOut = hornetApiService2.setEmailOptedOut(new AccountSetEmailOptedOutWrapper(z));
        Intrinsics.checkExpressionValueIsNotNull(emailOptedOut, "hornetApiService.setEmai…Wrapper(isEmailOptedOut))");
        return emailOptedOut;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable setAccountUsername(String str) {
        Intrinsics.checkParameterIsNotNull(str, "username");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable accountUsername = hornetApiService2.setAccountUsername(new AccountSetUsernameWrapper(str));
        Intrinsics.checkExpressionValueIsNotNull(accountUsername, "hornetApiService.setAcco…sernameWrapper(username))");
        return accountUsername;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable setAccountEmail(String str) {
        Intrinsics.checkParameterIsNotNull(str, "email");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable accountEmail = hornetApiService2.setAccountEmail(new AccountSetEmailWrapper(str));
        Intrinsics.checkExpressionValueIsNotNull(accountEmail, "hornetApiService.setAcco…ntSetEmailWrapper(email))");
        return accountEmail;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable setAccountPassword(String str) {
        Intrinsics.checkParameterIsNotNull(str, "password");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable accountPassword = hornetApiService2.setAccountPassword(new AccountSetPasswordWrapper(str));
        Intrinsics.checkExpressionValueIsNotNull(accountPassword, "hornetApiService.setAcco…asswordWrapper(password))");
        return accountPassword;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable setAccountPhone(String str) {
        Intrinsics.checkParameterIsNotNull(str, "phone");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable accountPhoneNumber = hornetApiService2.setAccountPhoneNumber(new AccountSetPhoneRequest(str));
        Intrinsics.checkExpressionValueIsNotNull(accountPhoneNumber, "hornetApiService.setAcco…ntSetPhoneRequest(phone))");
        return accountPhoneNumber;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable deleteAccount() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable deleteAccount = hornetApiService2.deleteAccount();
        Intrinsics.checkExpressionValueIsNotNull(deleteAccount, "hornetApiService.deleteAccount()");
        return deleteAccount;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable resendEmailVerification() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable resendEmailVerification = hornetApiService2.resendEmailVerification();
        Intrinsics.checkExpressionValueIsNotNull(resendEmailVerification, "hornetApiService.resendEmailVerification()");
        return resendEmailVerification;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable smsVerificationResend() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable smsVerificationResend = hornetApiService2.smsVerificationResend();
        Intrinsics.checkExpressionValueIsNotNull(smsVerificationResend, "hornetApiService.smsVerificationResend()");
        return smsVerificationResend;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable smsVerificationVerify(String str) {
        Intrinsics.checkParameterIsNotNull(str, "code");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable smsVerificationVerify = hornetApiService2.smsVerificationVerify(new SmsVerifyRequest(str));
        Intrinsics.checkExpressionValueIsNotNull(smsVerificationVerify, "hornetApiService.smsVeri…y(SmsVerifyRequest(code))");
        return smsVerificationVerify;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<FilterList> getFilters() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<FilterList> filters = hornetApiService2.getFilters();
        Intrinsics.checkExpressionValueIsNotNull(filters, "hornetApiService.filters");
        return filters;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable setFilters(FilterList filterList) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable filters = hornetApiService2.setFilters(filterList);
        Intrinsics.checkExpressionValueIsNotNull(filters, "hornetApiService.setFilters(filters)");
        return filters;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable setFilter(Filter filter) {
        Intrinsics.checkParameterIsNotNull(filter, "filter");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable filter2 = hornetApiService2.setFilter(FilterList.wrapFilter(filter));
        Intrinsics.checkExpressionValueIsNotNull(filter2, "hornetApiService.setFilt…rList.wrapFilter(filter))");
        return filter2;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable clearFiltersCategory(String str) {
        Intrinsics.checkParameterIsNotNull(str, "category");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable clearFilterCategory = hornetApiService2.clearFilterCategory(str);
        Intrinsics.checkExpressionValueIsNotNull(clearFilterCategory, "hornetApiService.clearFilterCategory(category)");
        return clearFilterCategory;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<LookupList> getLookupData() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<LookupList> lookupData = hornetApiService2.getLookupData();
        Intrinsics.checkExpressionValueIsNotNull(lookupData, "hornetApiService.lookupData");
        return lookupData;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable resetPassword(String str) {
        Intrinsics.checkParameterIsNotNull(str, "email");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable resetPassword = hornetApiService2.resetPassword(str);
        Intrinsics.checkExpressionValueIsNotNull(resetPassword, "hornetApiService.resetPassword(email)");
        return resetPassword;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable endSession() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable endSession = hornetApiService2.endSession();
        Intrinsics.checkExpressionValueIsNotNull(endSession, "hornetApiService.endSession()");
        return endSession;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<Totals> getUserTotals() {
        if (getSessionKernel().shouldRefreshUserTotals()) {
            HornetApiService hornetApiService2 = this.hornetApiService;
            if (hornetApiService2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
            }
            Single<Totals> userTotals = hornetApiService2.getUserTotals();
            Intrinsics.checkExpressionValueIsNotNull(userTotals, "hornetApiService.userTotals");
            return userTotals;
        }
        Single<Totals> just = Single.just(getSessionKernel().getTotals());
        Intrinsics.checkExpressionValueIsNotNull(just, "Single.just(sessionKernel.totals)");
        return just;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<LocationInfo> getGetLocationInfo() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<LocationInfo> locationInfo = hornetApiService2.getLocationInfo();
        Intrinsics.checkExpressionValueIsNotNull(locationInfo, "hornetApiService.locationInfo");
        return locationInfo;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<ConversationList> getConversations(int i, int i2, ChatsInteractor.Companion.Inbox inbox) {
        Intrinsics.checkParameterIsNotNull(inbox, AdScreens.INBOX);
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<ConversationList> conversations = hornetApiService2.getConversations(i, i2, inbox.getValue());
        Intrinsics.checkExpressionValueIsNotNull(conversations, "hornetApiService.getConv…erPage, inbox.getValue())");
        return conversations;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<ConversationMessages> getFullConversation(Long l, String str, int i) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        if (l == null) {
            Intrinsics.throwNpe();
        }
        Single<ConversationMessages> fullConversation = hornetApiService2.getFullConversation(KotlinHelpersKt.toUnsignedString$default(l.longValue(), 0, 1, null), str, i);
        Intrinsics.checkExpressionValueIsNotNull(fullConversation, "hornetApiService.getFull…(), beforeToken, perPage)");
        return fullConversation;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable markAllAsRead() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable markAllAsRead = hornetApiService2.markAllAsRead("");
        Intrinsics.checkExpressionValueIsNotNull(markAllAsRead, "hornetApiService.markAllAsRead(\"\")");
        return markAllAsRead;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable markAsRead(long j) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable markAsRead = hornetApiService2.markAsRead(KotlinHelpersKt.toUnsignedString$default(j, 0, 1, null));
        Intrinsics.checkExpressionValueIsNotNull(markAsRead, "hornetApiService.markAsRead(id.toUnsignedString())");
        return markAsRead;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable markInboxAsSeen(ChatsInteractor.Companion.Inbox inbox) {
        Intrinsics.checkParameterIsNotNull(inbox, AdScreens.INBOX);
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable markInboxSeen = hornetApiService2.markInboxSeen(inbox.getValue());
        Intrinsics.checkExpressionValueIsNotNull(markInboxSeen, "hornetApiService.markInboxSeen(inbox.getValue())");
        return markInboxSeen;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable deleteConversation(long j) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable deleteConversation = hornetApiService2.deleteConversation(KotlinHelpersKt.toUnsignedString$default(j, 0, 1, null));
        Intrinsics.checkExpressionValueIsNotNull(deleteConversation, "hornetApiService.deleteC…berId.toUnsignedString())");
        return deleteConversation;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<MessageResponse> deleteMessage(String str, long j) {
        Intrinsics.checkParameterIsNotNull(str, MqttServiceConstants.MESSAGE_ID);
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<MessageResponse> deleteMessage = hornetApiService2.deleteMessage(str, KotlinHelpersKt.toUnsignedString$default(j, 0, 1, null));
        Intrinsics.checkExpressionValueIsNotNull(deleteMessage, "hornetApiService.deleteM…berId.toUnsignedString())");
        return deleteMessage;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Pair<Single<TempPhotoWrapper>, Flowable<Pair<Long, Long>>> uploadPhoto(File file) {
        Intrinsics.checkParameterIsNotNull(file, "file");
        String fileExtensionFromUrl = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
        Intrinsics.checkExpressionValueIsNotNull(fileExtensionFromUrl, "it");
        if (StringsKt.isBlank(fileExtensionFromUrl)) {
            fileExtensionFromUrl = "jpg";
        }
        String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtensionFromUrl);
        if (mimeTypeFromExtension == null) {
            mimeTypeFromExtension = "image/jpeg";
        }
        CountingFileRequestBody countingFileRequestBody = new CountingFileRequestBody(file, MediaType.parse(mimeTypeFromExtension));
        MultipartBody.Builder type = new MultipartBody.Builder().setType(MultipartBody.FORM);
        MultipartBody build = type.addFormDataPart("temp_photo[photo][tempfile]", "image." + fileExtensionFromUrl, countingFileRequestBody).build();
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        return new Pair<>(hornetApiService2.uploadPhoto(build), countingFileRequestBody.getProgressFlowable());
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<ProfilePhotoUploadResult> uploadProfilePhoto(File file, boolean z, Rect rect, Rect rect2, int i, int i2) {
        Intrinsics.checkParameterIsNotNull(file, "file");
        Intrinsics.checkParameterIsNotNull(rect, "profile");
        Intrinsics.checkParameterIsNotNull(rect2, "avatar");
        Crashlytics.log(3, "HornetApp", "uploadProfilePhoto: " + file + ' ' + rect + ' ' + rect2);
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<ProfilePhotoUploadResult> uploadProfilePhoto = hornetApiService2.uploadProfilePhoto(PhotoRequestBodyFactory.INSTANCE.buildProfilePhotoRequestBody(file, z, rect, rect2, i, i2));
        Intrinsics.checkExpressionValueIsNotNull(uploadProfilePhoto, "hornetApiService\n\t\t\t\t.up…alWidth, originalHeight))");
        return uploadProfilePhoto;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<FeedPhotoUploadResult> uploadFeedPhoto(File file, Rect rect, int i, int i2) {
        Intrinsics.checkParameterIsNotNull(file, "file");
        Intrinsics.checkParameterIsNotNull(rect, MessengerShareContentUtility.IMAGE_RATIO_SQUARE);
        Crashlytics.log(3, "HornetApp", "uploadFeedPhoto: " + file + ' ' + rect);
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<FeedPhotoUploadResult> uploadFeedPhoto = hornetApiService2.uploadFeedPhoto(PhotoRequestBodyFactory.INSTANCE.buildFeedPhotoRequestBody(file, rect, i, i2));
        Intrinsics.checkExpressionValueIsNotNull(uploadFeedPhoto, "hornetApiService\n\t\t\t\t.up…alWidth, originalHeight))");
        return uploadFeedPhoto;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<FeedMomentUploadResult> uploadFeedMoment(File file, Rect rect, Integer num, Integer num2, String str, boolean z) {
        Crashlytics.log(3, "HornetApp", "uploadFeedPhoto: " + file + ' ' + rect);
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<FeedMomentUploadResult> uploadMoment = hornetApiService2.uploadMoment(PhotoRequestBodyFactory.INSTANCE.buildFeedMomentRequestBody(file, rect, num, num2, str, z));
        Intrinsics.checkExpressionValueIsNotNull(uploadMoment, "hornetApiService\n\t\t\t\t.up…t, caption, publicShare))");
        return uploadMoment;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable updatePhotoSlots(ArrayList<PhotoWrapper> arrayList) {
        Intrinsics.checkParameterIsNotNull(arrayList, "photoWrappers");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable updatePhotoSlots = hornetApiService2.updatePhotoSlots(UpdatePhotoIndexRequest.createUsing(arrayList));
        Intrinsics.checkExpressionValueIsNotNull(updatePhotoSlots, "hornetApiService.updateP…eateUsing(photoWrappers))");
        return updatePhotoSlots;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable removePhoto(String str) {
        Intrinsics.checkParameterIsNotNull(str, "id");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable removePhoto = hornetApiService2.removePhoto(str);
        Intrinsics.checkExpressionValueIsNotNull(removePhoto, "hornetApiService.removePhoto(id)");
        return removePhoto;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable changePhotoMode(String str, boolean z) {
        Intrinsics.checkParameterIsNotNull(str, "id");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable changePhotoMode = hornetApiService2.changePhotoMode(str, new UpdatePhotoModeRequest.Wrapper(new UpdatePhotoModeRequest(z)));
        Intrinsics.checkExpressionValueIsNotNull(changePhotoMode, "hornetApiService.changeP…toModeRequest(isPublic)))");
        return changePhotoMode;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<PhotosList> getMemberPhotoStream(long j, int i, int i2) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<PhotosList> memberPhotoStream = hornetApiService2.getMemberPhotoStream(KotlinHelpersKt.toUnsignedString$default(j, 0, 1, null), i, i2);
        Intrinsics.checkExpressionValueIsNotNull(memberPhotoStream, "hornetApiService.getMemb…dString(), page, perPage)");
        return memberPhotoStream;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<PhotosList> getMemberPrivatePhotos(long j, int i, int i2) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<PhotosList> memberPrivatePhotos = hornetApiService2.getMemberPrivatePhotos(KotlinHelpersKt.toUnsignedString$default(j, 0, 1, null), i, i2);
        Intrinsics.checkExpressionValueIsNotNull(memberPrivatePhotos, "hornetApiService.getMemb…dString(), page, perPage)");
        return memberPrivatePhotos;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<Channel> getMessagesChannel() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<Channel> messagesChannel = hornetApiService2.getMessagesChannel();
        Intrinsics.checkExpressionValueIsNotNull(messagesChannel, "hornetApiService.messagesChannel");
        return messagesChannel;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<ArrayList<Sticker>> getStickers() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<ArrayList<Sticker>> stickers = hornetApiService2.getStickers();
        Intrinsics.checkExpressionValueIsNotNull(stickers, "hornetApiService.stickers");
        return stickers;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<ArrayList<Honey>> getGemProducts() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<ArrayList<Honey>> gemProducts = hornetApiService2.getGemProducts();
        Intrinsics.checkExpressionValueIsNotNull(gemProducts, "hornetApiService.gemProducts");
        return gemProducts;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<CurrencyAccount> getGemAccount() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<R> map = hornetApiService2.getGemAccount().map(HornetApiClientImpl$getGemAccount$1.INSTANCE);
        Intrinsics.checkExpressionValueIsNotNull(map, "hornetApiService.gemAcco…pper -> wrapper.account }");
        return map;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<CurrencyAccount> getGemBalance() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<R> map = hornetApiService2.getGemBalance().map(HornetApiClientImpl$getGemBalance$1.INSTANCE);
        Intrinsics.checkExpressionValueIsNotNull(map, "hornetApiService.gemBala…pper -> wrapper.account }");
        return map;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<CurrencyAccountWrapper> getGemTransactions(int i, int i2) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<CurrencyAccountWrapper> gemTransactions = hornetApiService2.getGemTransactions(i, i2);
        Intrinsics.checkExpressionValueIsNotNull(gemTransactions, "hornetApiService.getGemTransactions(page, perPage)");
        return gemTransactions;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<HoneyShopProductsWrapper> getHoneyShopProducts(List<Long> list) {
        Intrinsics.checkParameterIsNotNull(list, "productIds");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<HoneyShopProductsWrapper> honeyShopProducts = hornetApiService2.getHoneyShopProducts(CollectionsKt.joinToString$default(list, ",", null, null, 0, null, null, 62, null));
        Intrinsics.checkExpressionValueIsNotNull(honeyShopProducts, "hornetApiService.getHone…uctIds.joinToString(\",\"))");
        return honeyShopProducts;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<HoneyShopPurchaseWrapper> purchaseHoneyShopProduct(long j) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<HoneyShopPurchaseWrapper> purchaseHoneyShopProduct = hornetApiService2.purchaseHoneyShopProduct(String.valueOf(j));
        Intrinsics.checkExpressionValueIsNotNull(purchaseHoneyShopProduct, "hornetApiService.purchas…uct(productId.toString())");
        return purchaseHoneyShopProduct;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<ArrayList<Subscription>> getPremiumMemberships() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<ArrayList<Subscription>> premiumMemberships = hornetApiService2.getPremiumMemberships();
        Intrinsics.checkExpressionValueIsNotNull(premiumMemberships, "hornetApiService.premiumMemberships");
        return premiumMemberships;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<ArrayList<Subscription>> getBranchRedeemablePremiumMemberships() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<ArrayList<Subscription>> branchRedeemablePremiumMemberships = hornetApiService2.getBranchRedeemablePremiumMemberships();
        Intrinsics.checkExpressionValueIsNotNull(branchRedeemablePremiumMemberships, "hornetApiService.branchR…eemablePremiumMemberships");
        return branchRedeemablePremiumMemberships;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable doPremiumMembershipTransaction(HornetTransactionWrapper hornetTransactionWrapper) {
        Intrinsics.checkParameterIsNotNull(hornetTransactionWrapper, "transactionWrapper");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable doPremiumMembershipTransaction = hornetApiService2.doPremiumMembershipTransaction(hornetTransactionWrapper);
        Intrinsics.checkExpressionValueIsNotNull(doPremiumMembershipTransaction, "hornetApiService.doPremi…ction(transactionWrapper)");
        return doPremiumMembershipTransaction;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable doBranchRedeemPremiumMembershipTransaction(String str) {
        Intrinsics.checkParameterIsNotNull(str, "productId");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable doBranchRedeemPremiumMembershipTransaction = hornetApiService2.doBranchRedeemPremiumMembershipTransaction(new BranchTransactionRequest.Wrapper(new BranchTransactionRequest(str)));
        Intrinsics.checkExpressionValueIsNotNull(doBranchRedeemPremiumMembershipTransaction, "hornetApiService.doBranc…ctionRequest(productId)))");
        return doBranchRedeemPremiumMembershipTransaction;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<FavouriteResponse> followMember(long j) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<FavouriteResponse> addFavourite = hornetApiService2.addFavourite(new AddFavouriteRequest(KotlinHelpersKt.toUnsignedString$default(j, 0, 1, null)));
        Intrinsics.checkExpressionValueIsNotNull(addFavourite, "hornetApiService.addFavo…t(id.toUnsignedString()))");
        return addFavourite;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable unfollowMember(long j) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable removeFavourite = hornetApiService2.removeFavourite(KotlinHelpersKt.toUnsignedString$default(j, 0, 1, null));
        Intrinsics.checkExpressionValueIsNotNull(removeFavourite, "hornetApiService.removeF…te(id.toUnsignedString())");
        return removeFavourite;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable blockUser(Long l) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        if (l == null) {
            Intrinsics.throwNpe();
        }
        Completable delay = hornetApiService2.blockUser(new BlockRequest(KotlinHelpersKt.toUnsignedString$default(l.longValue(), 0, 1, null))).delay(5, TimeUnit.SECONDS);
        Intrinsics.checkExpressionValueIsNotNull(delay, "hornetApiService.blockUs…lay(5L, TimeUnit.SECONDS)");
        return delay;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable reportUser(ReportRequest reportRequest) {
        Intrinsics.checkParameterIsNotNull(reportRequest, ShareConstants.WEB_DIALOG_RESULT_PARAM_REQUEST_ID);
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable reportUser = hornetApiService2.reportUser(reportRequest);
        Intrinsics.checkExpressionValueIsNotNull(reportUser, "hornetApiService.reportUser(request)");
        return reportUser;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<MemberList> getViewedMe(int i, int i2) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<MemberList> viewedMe = hornetApiService2.getViewedMe(i, i2);
        Intrinsics.checkExpressionValueIsNotNull(viewedMe, "hornetApiService.getViewedMe(page, perPage)");
        return viewedMe;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable addMemberNote(MemberNoteWrapper memberNoteWrapper) {
        Intrinsics.checkParameterIsNotNull(memberNoteWrapper, "note");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable addMemberNote = hornetApiService2.addMemberNote(memberNoteWrapper);
        Intrinsics.checkExpressionValueIsNotNull(addMemberNote, "hornetApiService.addMemberNote(note)");
        return addMemberNote;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<MemberList> getNearby(int i, int i2) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<MemberList> nearby = hornetApiService2.getNearby(i, i2);
        Intrinsics.checkExpressionValueIsNotNull(nearby, "hornetApiService.getNearby(page, perPage)");
        return nearby;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<MemberList> getFollowCandidates(int i, int i2) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<MemberList> followCandidates = hornetApiService2.getFollowCandidates(i, i2);
        Intrinsics.checkExpressionValueIsNotNull(followCandidates, "hornetApiService.getFoll…Candidates(page, perPage)");
        return followCandidates;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public void updateLocation(Location location) {
        Intrinsics.checkParameterIsNotNull(location, "location");
        this.latlng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<HashtagsListWrapper> getPopularHashtags() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<HashtagsListWrapper> popularHashtags = hornetApiService2.getPopularHashtags();
        Intrinsics.checkExpressionValueIsNotNull(popularHashtags, "hornetApiService.popularHashtags");
        return popularHashtags;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<HashtagsListWrapper> getHashtagSuggestions(String str) {
        Intrinsics.checkParameterIsNotNull(str, "query");
        if (TextUtils.isEmpty(str)) {
            return getPopularHashtags();
        }
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<HashtagsListWrapper> hashtagSuggestions = hornetApiService2.getHashtagSuggestions(str);
        Intrinsics.checkExpressionValueIsNotNull(hashtagSuggestions, "hornetApiService.getHashtagSuggestions(query)");
        return hashtagSuggestions;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<HashtagsListWrapper> getInterestsHashtags() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<HashtagsListWrapper> interestsHashtags = hornetApiService2.getInterestsHashtags();
        Intrinsics.checkExpressionValueIsNotNull(interestsHashtags, "hornetApiService.interestsHashtags");
        return interestsHashtags;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<MemberList> searchUsernames(String str, int i, int i2) {
        Intrinsics.checkParameterIsNotNull(str, "username");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<MemberList> searchUsernames = hornetApiService2.searchUsernames(str, i, i2);
        Intrinsics.checkExpressionValueIsNotNull(searchUsernames, "hornetApiService.searchU…(username, page, perPage)");
        return searchUsernames;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<MemberList> searchHashtags(String str, int i, int i2) {
        Intrinsics.checkParameterIsNotNull(str, "hashtags");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<MemberList> searchHashtags = hornetApiService2.searchHashtags(str, i, i2);
        Intrinsics.checkExpressionValueIsNotNull(searchHashtags, "hornetApiService.searchH…(hashtags, page, perPage)");
        return searchHashtags;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<BlockList> getBlockedUsers(int i, int i2) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<BlockList> blockedUsers = hornetApiService2.getBlockedUsers(i, i2);
        Intrinsics.checkExpressionValueIsNotNull(blockedUsers, "hornetApiService.getBlockedUsers(page, perPage)");
        return blockedUsers;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable removeBlock(Long l) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        if (l == null) {
            Intrinsics.throwNpe();
        }
        Completable removeBlock = hornetApiService2.removeBlock(KotlinHelpersKt.toUnsignedString$default(l.longValue(), 0, 1, null));
        Intrinsics.checkExpressionValueIsNotNull(removeBlock, "hornetApiService.removeB…rId!!.toUnsignedString())");
        return removeBlock;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable removeAllBlocks() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable removeAllBlocks = hornetApiService2.removeAllBlocks();
        Intrinsics.checkExpressionValueIsNotNull(removeAllBlocks, "hornetApiService.removeAllBlocks()");
        return removeAllBlocks;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<PhotoPermissionList> getPhotoPermissions(int i, int i2) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<PhotoPermissionList> photoPermissions = hornetApiService2.getPhotoPermissions(i, i2);
        Intrinsics.checkExpressionValueIsNotNull(photoPermissions, "hornetApiService.getPhot…ermissions(page, perPage)");
        return photoPermissions;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable revokeAllPhotoPermissions() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable revokeAllPhotoPermissions = hornetApiService2.revokeAllPhotoPermissions();
        Intrinsics.checkExpressionValueIsNotNull(revokeAllPhotoPermissions, "hornetApiService.revokeAllPhotoPermissions()");
        return revokeAllPhotoPermissions;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<MessageResponse> sendMessage(Message<?> message) {
        Intrinsics.checkParameterIsNotNull(message, "message");
        SessionKernel sessionKernel2 = getSessionKernel();
        if (sessionKernel2 == null) {
            Intrinsics.throwNpe();
        }
        SessionData.Session session = sessionKernel2.getSession();
        if (session == null) {
            Intrinsics.throwNpe();
        }
        FullMemberWrapper.FullMember profile = session.getProfile();
        if (profile == null) {
            Intrinsics.throwNpe();
        }
        Long l = profile.id;
        if (l == null) {
            Intrinsics.throwNpe();
        }
        Object createDeviceSignature = createDeviceSignature(String.valueOf(l.longValue()));
        if (createDeviceSignature != null) {
            KeyUtil.Special special = (KeyUtil.Special) createDeviceSignature;
            HornetApiService hornetApiService2 = this.hornetApiService;
            if (hornetApiService2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
            }
            Single<MessageResponse> sendMessageObservable = hornetApiService2.sendMessageObservable(new MessageObjectWrapper(message, special.getS(), special.getN(), special.getT()));
            Intrinsics.checkExpressionValueIsNotNull(sendMessageObservable, "hornetApiService.sendMes…s, special.n, special.t))");
            return sendMessageObservable;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.hornet.android.utils.KeyUtil.Special");
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable sendReadReceiptMessage(OutgoingReadReceiptMessage outgoingReadReceiptMessage) {
        Intrinsics.checkParameterIsNotNull(outgoingReadReceiptMessage, "message");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable sendReadReceiptMessage = hornetApiService2.sendReadReceiptMessage(outgoingReadReceiptMessage);
        Intrinsics.checkExpressionValueIsNotNull(sendReadReceiptMessage, "hornetApiService.sendReadReceiptMessage(message)");
        return sendReadReceiptMessage;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable sendViewedProfiles(ViewedMeRequest viewedMeRequest) {
        Intrinsics.checkParameterIsNotNull(viewedMeRequest, "viewed");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable sendViewedProfiles = hornetApiService2.sendViewedProfiles(viewedMeRequest);
        Intrinsics.checkExpressionValueIsNotNull(sendViewedProfiles, "hornetApiService.sendViewedProfiles(viewed)");
        return sendViewedProfiles;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable sendFeedback(FeedbackRequest feedbackRequest) {
        Intrinsics.checkParameterIsNotNull(feedbackRequest, "feedbackRequest");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable sendFeedback = hornetApiService2.sendFeedback(feedbackRequest);
        Intrinsics.checkExpressionValueIsNotNull(sendFeedback, "hornetApiService.sendFeedback(feedbackRequest)");
        return sendFeedback;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable initKernels() {
        Completable andThen = getLookupKernel().initialize().andThen(getEntitlementKernel().initialize()).andThen(getChatsInteractor().initialize()).andThen(getFilterKernel().initialize()).andThen(getWalletInteractor().initialize()).andThen(getProductInteractor().initialize()).andThen(getMqttKernel().initialize()).andThen(Completable.fromAction(new HornetApiClientImpl$initKernels$1(this))).andThen(EmojiCompatKernel.INSTANCE.get(this.context)).andThen(Completable.fromAction(HornetApiClientImpl$initKernels$2.INSTANCE)).andThen(Completable.fromAction(new HornetApiClientImpl$initKernels$3(this)));
        Intrinsics.checkExpressionValueIsNotNull(andThen, "lookupKernel.initialize(…rnelInitRun = true\n\t\t\t\t})");
        return andThen;
    }

    /* access modifiers changed from: private */
    public final void setUserProperty(String str, String str2) {
        FirebaseAnalytics firebaseAnalytics2 = this.firebaseAnalytics;
        if (firebaseAnalytics2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("firebaseAnalytics");
        }
        firebaseAnalytics2.setUserProperty(str, str2);
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<Activities.Wrapper> getTimelineFeedAfterToken(String str, int i) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<Activities.Wrapper> timelineFeedAfterToken = hornetApiService2.getTimelineFeedAfterToken(str, i);
        Intrinsics.checkExpressionValueIsNotNull(timelineFeedAfterToken, "hornetApiService.getTime…oken(afterToken, perPage)");
        return timelineFeedAfterToken;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<Activities.Wrapper> getTimelineFeedBeforeToken(String str, int i) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<Activities.Wrapper> timelineFeedBeforeToken = hornetApiService2.getTimelineFeedBeforeToken(str, i);
        Intrinsics.checkExpressionValueIsNotNull(timelineFeedBeforeToken, "hornetApiService.getTime…ken(beforeToken, perPage)");
        return timelineFeedBeforeToken;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<Activities.Wrapper> getMemberTimelineFeedAfterToken(long j, String str, int i) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<Activities.Wrapper> memberTimelineFeedAfterToken = hornetApiService2.getMemberTimelineFeedAfterToken(KotlinHelpersKt.toUnsignedString$default(j, 0, 1, null), str, i);
        Intrinsics.checkExpressionValueIsNotNull(memberTimelineFeedAfterToken, "hornetApiService.getMemb…g(), afterToken, perPage)");
        return memberTimelineFeedAfterToken;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<Activities.Wrapper> getMemberTimelineFeedBeforeToken(long j, String str, int i) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<Activities.Wrapper> memberTimelineFeedBeforeToken = hornetApiService2.getMemberTimelineFeedBeforeToken(KotlinHelpersKt.toUnsignedString$default(j, 0, 1, null), str, i);
        Intrinsics.checkExpressionValueIsNotNull(memberTimelineFeedBeforeToken, "hornetApiService.getMemb…(), beforeToken, perPage)");
        return memberTimelineFeedBeforeToken;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<MemberList> getMemberForDynamicGrid(String str, int i, int i2) {
        Intrinsics.checkParameterIsNotNull(str, "grid");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<MemberList> memberForDynamicGrid = hornetApiService2.getMemberForDynamicGrid(str, i, i2);
        Intrinsics.checkExpressionValueIsNotNull(memberForDynamicGrid, "hornetApiService.getMemb…Grid(grid, page, perPage)");
        return memberForDynamicGrid;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<Activities.Wrapper> getNotificationsAfterToken(String str, int i) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<Activities.Wrapper> notificationsAfterToken = hornetApiService2.getNotificationsAfterToken(str, i);
        Intrinsics.checkExpressionValueIsNotNull(notificationsAfterToken, "hornetApiService.getNoti…oken(afterToken, perPage)");
        return notificationsAfterToken;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<Activities.Wrapper> getDynamicFeed(String str, String str2, int i) {
        Intrinsics.checkParameterIsNotNull(str, "path");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<Activities.Wrapper> dynamicFeed = hornetApiService2.getDynamicFeed(str, str2, i);
        Intrinsics.checkExpressionValueIsNotNull(dynamicFeed, "hornetApiService.getDyna…ath, afterToken, perPage)");
        return dynamicFeed;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable reactToActivity(String str, ContentLike contentLike) {
        Intrinsics.checkParameterIsNotNull(str, "activityId");
        Intrinsics.checkParameterIsNotNull(contentLike, "reaction");
        SessionKernel sessionKernel2 = getSessionKernel();
        if (sessionKernel2 == null) {
            Intrinsics.throwNpe();
        }
        SessionData.Session session = sessionKernel2.getSession();
        if (session == null) {
            Intrinsics.throwNpe();
        }
        FullMemberWrapper.FullMember profile = session.getProfile();
        if (profile == null) {
            Intrinsics.throwNpe();
        }
        Long l = profile.id;
        if (l == null) {
            Intrinsics.throwNpe();
        }
        Object createDeviceSignature = createDeviceSignature(String.valueOf(l.longValue()));
        if (createDeviceSignature != null) {
            contentLike.sign((KeyUtil.Special) createDeviceSignature);
            HornetApiService hornetApiService2 = this.hornetApiService;
            if (hornetApiService2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
            }
            Completable reactToActivity = hornetApiService2.reactToActivity(str, contentLike);
            Intrinsics.checkExpressionValueIsNotNull(reactToActivity, "hornetApiService.reactTo…ity(activityId, reaction)");
            return reactToActivity;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.hornet.android.utils.KeyUtil.Special");
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<Activities.Activity.Wrapper> getActivity(String str) {
        Intrinsics.checkParameterIsNotNull(str, "activityId");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<Activities.Activity.Wrapper> activity = hornetApiService2.getActivity(str);
        Intrinsics.checkExpressionValueIsNotNull(activity, "hornetApiService.getActivity(activityId)");
        return activity;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable deleteOwnActivity(String str) {
        Intrinsics.checkParameterIsNotNull(str, "activityId");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable deleteOwnActivity = hornetApiService2.deleteOwnActivity(str);
        Intrinsics.checkExpressionValueIsNotNull(deleteOwnActivity, "hornetApiService.deleteOwnActivity(activityId)");
        return deleteOwnActivity;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable reportActivity(String str, ReportRequest reportRequest) {
        Intrinsics.checkParameterIsNotNull(str, "activityId");
        Intrinsics.checkParameterIsNotNull(reportRequest, "report");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable reportActivity = hornetApiService2.reportActivity(str, reportRequest);
        Intrinsics.checkExpressionValueIsNotNull(reportActivity, "hornetApiService.reportA…ivity(activityId, report)");
        return reportActivity;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable ignoreActivityMember(MemberIdWrapper memberIdWrapper) {
        Intrinsics.checkParameterIsNotNull(memberIdWrapper, "memberId");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable ignoreActivityMember = hornetApiService2.ignoreActivityMember(memberIdWrapper);
        Intrinsics.checkExpressionValueIsNotNull(ignoreActivityMember, "hornetApiService.ignoreActivityMember(memberId)");
        return ignoreActivityMember;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<CommentsWrapper> getActivityCommentsAfterToken(String str, String str2, int i) {
        Intrinsics.checkParameterIsNotNull(str, "activityId");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<CommentsWrapper> activityCommentsAfterToken = hornetApiService2.getActivityCommentsAfterToken(str, str2, i);
        Intrinsics.checkExpressionValueIsNotNull(activityCommentsAfterToken, "hornetApiService.getActi…tivityId, token, perPage)");
        return activityCommentsAfterToken;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<CommentsWrapper> getActivityCommentsBeforeToken(String str, String str2, int i) {
        Intrinsics.checkParameterIsNotNull(str, "activityId");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<CommentsWrapper> activityCommentsBeforeToken = hornetApiService2.getActivityCommentsBeforeToken(str, str2, i);
        Intrinsics.checkExpressionValueIsNotNull(activityCommentsBeforeToken, "hornetApiService.getActi…tivityId, token, perPage)");
        return activityCommentsBeforeToken;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<Comment.Wrapper> postComment(String str, String str2) {
        Intrinsics.checkParameterIsNotNull(str, "activityId");
        Intrinsics.checkParameterIsNotNull(str2, "body");
        CommentWrapper commentWrapper = new CommentWrapper(new TextCommentBody(str2));
        SessionKernel sessionKernel2 = getSessionKernel();
        if (sessionKernel2 == null) {
            Intrinsics.throwNpe();
        }
        SessionData.Session session = sessionKernel2.getSession();
        if (session == null) {
            Intrinsics.throwNpe();
        }
        FullMemberWrapper.FullMember profile = session.getProfile();
        if (profile == null) {
            Intrinsics.throwNpe();
        }
        Long l = profile.id;
        if (l == null) {
            Intrinsics.throwNpe();
        }
        Object createDeviceSignature = createDeviceSignature(String.valueOf(l.longValue()));
        if (createDeviceSignature != null) {
            commentWrapper.sign((KeyUtil.Special) createDeviceSignature);
            HornetApiService hornetApiService2 = this.hornetApiService;
            if (hornetApiService2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
            }
            Single<Comment.Wrapper> postComment = hornetApiService2.postComment(str, commentWrapper);
            Intrinsics.checkExpressionValueIsNotNull(postComment, "hornetApiService.postComment(activityId, comment)");
            return postComment;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.hornet.android.utils.KeyUtil.Special");
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable likeComment(String str, long j, ContentLike contentLike) {
        Intrinsics.checkParameterIsNotNull(str, "activityId");
        Intrinsics.checkParameterIsNotNull(contentLike, "liked");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable likeComment = hornetApiService2.likeComment(str, Long.valueOf(j), contentLike);
        Intrinsics.checkExpressionValueIsNotNull(likeComment, "hornetApiService.likeCom…vityId, commentId, liked)");
        return likeComment;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<Comment.Wrapper> editComment(String str, long j, String str2) {
        Intrinsics.checkParameterIsNotNull(str, "activityId");
        Intrinsics.checkParameterIsNotNull(str2, "body");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<Comment.Wrapper> editComment = hornetApiService2.editComment(str, Long.valueOf(j), new TextCommentBody(str2));
        Intrinsics.checkExpressionValueIsNotNull(editComment, "hornetApiService.editCom…d, TextCommentBody(body))");
        return editComment;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable deleteComment(String str, long j) {
        Intrinsics.checkParameterIsNotNull(str, "activityId");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable deleteComment = hornetApiService2.deleteComment(str, Long.valueOf(j));
        Intrinsics.checkExpressionValueIsNotNull(deleteComment, "hornetApiService.deleteC…nt(activityId, commentId)");
        return deleteComment;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable reportComment(String str, long j, ReportRequest reportRequest) {
        Intrinsics.checkParameterIsNotNull(str, "activityId");
        Intrinsics.checkParameterIsNotNull(reportRequest, "report");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable reportComment = hornetApiService2.reportComment(str, Long.valueOf(j), reportRequest);
        Intrinsics.checkExpressionValueIsNotNull(reportComment, "hornetApiService.reportC…ityId, commentId, report)");
        return reportComment;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<DiscoverResponse> getDiscover() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<DiscoverResponse> discover = hornetApiService2.getDiscover();
        Intrinsics.checkExpressionValueIsNotNull(discover, "hornetApiService.discover");
        return discover;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<PlacesWrapper> discoverPlaces(int i, int i2) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<PlacesWrapper> discoverPlaces = hornetApiService2.discoverPlaces(i, i2);
        Intrinsics.checkExpressionValueIsNotNull(discoverPlaces, "hornetApiService.discoverPlaces(page, perPage)");
        return discoverPlaces;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable reactToPlace(String str, ContentLike contentLike) {
        Intrinsics.checkParameterIsNotNull(str, "placeId");
        Intrinsics.checkParameterIsNotNull(contentLike, "reaction");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable reactToPlace = hornetApiService2.reactToPlace(str, contentLike);
        Intrinsics.checkExpressionValueIsNotNull(reactToPlace, "hornetApiService.reactToPlace(placeId, reaction)");
        return reactToPlace;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<Place.Wrapper> getPlaceById(String str) {
        Intrinsics.checkParameterIsNotNull(str, "placeId");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<Place.Wrapper> placeById = hornetApiService2.getPlaceById(str);
        Intrinsics.checkExpressionValueIsNotNull(placeById, "hornetApiService.getPlaceById(placeId)");
        return placeById;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<EventsWrapper> discoverEvents(int i, int i2) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<EventsWrapper> discoverEvents = hornetApiService2.discoverEvents(i, i2);
        Intrinsics.checkExpressionValueIsNotNull(discoverEvents, "hornetApiService.discoverEvents(page, perPage)");
        return discoverEvents;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable reactToEvent(String str, ContentLike contentLike) {
        Intrinsics.checkParameterIsNotNull(str, "eventId");
        Intrinsics.checkParameterIsNotNull(contentLike, "reaction");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable reactToEvent = hornetApiService2.reactToEvent(str, contentLike);
        Intrinsics.checkExpressionValueIsNotNull(reactToEvent, "hornetApiService.reactToEvent(eventId, reaction)");
        return reactToEvent;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<Event.Wrapper> getEventById(String str) {
        Intrinsics.checkParameterIsNotNull(str, "eventId");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<Event.Wrapper> eventById = hornetApiService2.getEventById(str);
        Intrinsics.checkExpressionValueIsNotNull(eventById, "hornetApiService.getEventById(eventId)");
        return eventById;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<Stories.Wrapper> discoverStories(String str, int i, int i2) {
        Intrinsics.checkParameterIsNotNull(str, "flavour");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<Stories.Wrapper> discoverStories = hornetApiService2.discoverStories(str, i, i2);
        Intrinsics.checkExpressionValueIsNotNull(discoverStories, "hornetApiService.discove…s(flavour, page, perPage)");
        return discoverStories;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable reactToStory(long j, ContentLike contentLike) {
        Intrinsics.checkParameterIsNotNull(contentLike, "reaction");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable reactToStory = hornetApiService2.reactToStory(KotlinHelpersKt.toUnsignedString$default(j, 0, 1, null), contentLike);
        Intrinsics.checkExpressionValueIsNotNull(reactToStory, "hornetApiService.reactTo…signedString(), reaction)");
        return reactToStory;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable trackStoryView(long j) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable trackStoryView = hornetApiService2.trackStoryView(KotlinHelpersKt.toUnsignedString$default(j, 0, 1, null));
        Intrinsics.checkExpressionValueIsNotNull(trackStoryView, "hornetApiService.trackSt…oryId.toUnsignedString())");
        return trackStoryView;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<Story.Wrapper> getStoryById(long j) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<Story.Wrapper> storyById = hornetApiService2.getStoryById(KotlinHelpersKt.toUnsignedString$default(j, 0, 1, null));
        Intrinsics.checkExpressionValueIsNotNull(storyById, "hornetApiService.getStor…oryId.toUnsignedString())");
        return storyById;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<Story.Wrapper> getStoryBySlug(String str) {
        Intrinsics.checkParameterIsNotNull(str, "storySlug");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<Story.Wrapper> storyBySlug = hornetApiService2.getStoryBySlug(str);
        Intrinsics.checkExpressionValueIsNotNull(storyBySlug, "hornetApiService.getStoryBySlug(storySlug)");
        return storyBySlug;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable shareStory(long j, StoryShare storyShare) {
        Intrinsics.checkParameterIsNotNull(storyShare, "body");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable shareStory = hornetApiService2.shareStory(KotlinHelpersKt.toUnsignedString$default(j, 0, 1, null), storyShare);
        Intrinsics.checkExpressionValueIsNotNull(shareStory, "hornetApiService.shareSt…toUnsignedString(), body)");
        return shareStory;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable shareActivity(String str, ActivityShare activityShare) {
        Intrinsics.checkParameterIsNotNull(str, "activityId");
        Intrinsics.checkParameterIsNotNull(activityShare, "body");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable shareActivity = hornetApiService2.shareActivity(str, activityShare);
        Intrinsics.checkExpressionValueIsNotNull(shareActivity, "hornetApiService.shareActivity(activityId, body)");
        return shareActivity;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable reactTo(String str, String str2, ContentLike contentLike) {
        Intrinsics.checkParameterIsNotNull(str, OstDeviceManagerOperation.KIND);
        Intrinsics.checkParameterIsNotNull(str2, "id");
        Intrinsics.checkParameterIsNotNull(contentLike, "reaction");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable reactTo = hornetApiService2.reactTo(str, str2, contentLike);
        Intrinsics.checkExpressionValueIsNotNull(reactTo, "hornetApiService.reactTo(kind, id, reaction)");
        return reactTo;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<MemberList> getSpecificListMembers(String str, String str2, int i, int i2) {
        Intrinsics.checkParameterIsNotNull(str, OstDeviceManagerOperation.KIND);
        Intrinsics.checkParameterIsNotNull(str2, "id");
        if (str.hashCode() == 2048605165 && str.equals("activities")) {
            HornetApiService hornetApiService2 = this.hornetApiService;
            if (hornetApiService2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
            }
            Single<MemberList> activityMembers = hornetApiService2.getActivityMembers(str2, i, i2);
            Intrinsics.checkExpressionValueIsNotNull(activityMembers, "hornetApiService.getActi…embers(id, page, perPage)");
            return activityMembers;
        }
        HornetApiService hornetApiService3 = this.hornetApiService;
        if (hornetApiService3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<MemberList> specificListMembers = hornetApiService3.getSpecificListMembers(str, str2, i, i2);
        Intrinsics.checkExpressionValueIsNotNull(specificListMembers, "hornetApiService.getSpec…(kind, id, page, perPage)");
        return specificListMembers;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable verifyCaptcha(String str) {
        Intrinsics.checkParameterIsNotNull(str, OstSdk.TOKEN);
        CaptchaResponse captchaResponse = new CaptchaResponse(str);
        SessionKernel sessionKernel2 = getSessionKernel();
        if (sessionKernel2 == null) {
            Intrinsics.throwNpe();
        }
        SessionData.Session session = sessionKernel2.getSession();
        if (session == null) {
            Intrinsics.throwNpe();
        }
        FullMemberWrapper.FullMember profile = session.getProfile();
        if (profile == null) {
            Intrinsics.throwNpe();
        }
        Long l = profile.id;
        if (l == null) {
            Intrinsics.throwNpe();
        }
        Object createDeviceSignature = createDeviceSignature(String.valueOf(l.longValue()));
        if (createDeviceSignature != null) {
            captchaResponse.sign((KeyUtil.Special) createDeviceSignature);
            HornetApiService hornetApiService2 = this.hornetApiService;
            if (hornetApiService2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
            }
            Completable verify = hornetApiService2.verify(captchaResponse);
            Intrinsics.checkExpressionValueIsNotNull(verify, "hornetApiService.verify(captcha)");
            return verify;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.hornet.android.utils.KeyUtil.Special");
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable confirmFeedCampaign(String str) {
        Intrinsics.checkParameterIsNotNull(str, "endpoint");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable confirmFeedCampaign = hornetApiService2.confirmFeedCampaign(str);
        Intrinsics.checkExpressionValueIsNotNull(confirmFeedCampaign, "hornetApiService.confirmFeedCampaign(endpoint)");
        return confirmFeedCampaign;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable removeUserWaitingForResponse(String str) {
        Intrinsics.checkParameterIsNotNull(str, "id");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable removeUserWaitingForResponse = hornetApiService2.removeUserWaitingForResponse(str);
        Intrinsics.checkExpressionValueIsNotNull(removeUserWaitingForResponse, "hornetApiService.removeUserWaitingForResponse(id)");
        return removeUserWaitingForResponse;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<WalletSessionData.WalletSessionWrapper> walletLogin() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<WalletSessionData.WalletSessionWrapper> walletLogin = hornetApiService2.walletLogin();
        Intrinsics.checkExpressionValueIsNotNull(walletLogin, "hornetApiService.walletLogin()");
        return walletLogin;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<WalletPassphrase.WalletPassphraseWrapper> walletPassphrase() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<WalletPassphrase.WalletPassphraseWrapper> walletPassphrase = hornetApiService2.walletPassphrase();
        Intrinsics.checkExpressionValueIsNotNull(walletPassphrase, "hornetApiService.walletPassphrase()");
        return walletPassphrase;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<WalletDevice.WalletDeviceWrapper> walletRegisterDevice(WalletDevice.WalletDeviceWrapper walletDeviceWrapper) {
        Intrinsics.checkParameterIsNotNull(walletDeviceWrapper, "deviceWrapper");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<WalletDevice.WalletDeviceWrapper> walletRegisterDevice = hornetApiService2.walletRegisterDevice(walletDeviceWrapper);
        Intrinsics.checkExpressionValueIsNotNull(walletRegisterDevice, "hornetApiService.walletR…sterDevice(deviceWrapper)");
        return walletRegisterDevice;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<WalletTransactionsList> walletTransactions(String str, int i, int i2) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<WalletTransactionsList> walletTransactions = hornetApiService2.walletTransactions(str, i, i2);
        Intrinsics.checkExpressionValueIsNotNull(walletTransactions, "hornetApiService.walletT…ns(status, page, perPage)");
        return walletTransactions;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<LedgerTransaction.LedgerWrapper> walletLedgerTransaction(String str, int i) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<LedgerTransaction.LedgerWrapper> walletLedgerTransactions = hornetApiService2.walletLedgerTransactions(str, i);
        Intrinsics.checkExpressionValueIsNotNull(walletLedgerTransactions, "hornetApiService.walletL…sactions(before, perPage)");
        return walletLedgerTransactions;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<WalletBalance.WalletBalanceWrapper> walletBalance() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<WalletBalance.WalletBalanceWrapper> walletBalance = hornetApiService2.walletBalance();
        Intrinsics.checkExpressionValueIsNotNull(walletBalance, "hornetApiService.walletBalance()");
        return walletBalance;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<WalletTransactionsList.WalletTransactionWrapper> walletTransaction(long j) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<WalletTransactionsList.WalletTransactionWrapper> walletTransaction = hornetApiService2.walletTransaction(String.valueOf(j));
        Intrinsics.checkExpressionValueIsNotNull(walletTransaction, "hornetApiService.walletT…transactionId.toString())");
        return walletTransaction;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<WalletTransactionsList.WalletTransactionWrapper> confirmTransaction(String str, OstWalletTransaction.OstWalletTransactionWrapper ostWalletTransactionWrapper) {
        Intrinsics.checkParameterIsNotNull(str, "id");
        Intrinsics.checkParameterIsNotNull(ostWalletTransactionWrapper, "ostTransaction");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<WalletTransactionsList.WalletTransactionWrapper> transactionComplete = hornetApiService2.transactionComplete(str, ostWalletTransactionWrapper);
        Intrinsics.checkExpressionValueIsNotNull(transactionComplete, "hornetApiService.transac…plete(id, ostTransaction)");
        return transactionComplete;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<WalletTransactionsList.WalletTransactionWrapper> declineTransaction(String str) {
        Intrinsics.checkParameterIsNotNull(str, "id");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<WalletTransactionsList.WalletTransactionWrapper> transactionDeclined = hornetApiService2.transactionDeclined(str);
        Intrinsics.checkExpressionValueIsNotNull(transactionDeclined, "hornetApiService.transactionDeclined(id)");
        return transactionDeclined;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<WalletDevice.WalletDeviceWrapper> recoverDevice() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<WalletDevice.WalletDeviceWrapper> deviceRecovery = hornetApiService2.deviceRecovery();
        Intrinsics.checkExpressionValueIsNotNull(deviceRecovery, "hornetApiService.deviceRecovery()");
        return deviceRecovery;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<FeedPhotoWrapper.FeedPhotoListWrapper> getFeedPhotos(int i, int i2, long j) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<FeedPhotoWrapper.FeedPhotoListWrapper> feedPhotos = hornetApiService2.getFeedPhotos(i, i2, Long.valueOf(j));
        Intrinsics.checkExpressionValueIsNotNull(feedPhotos, "hornetApiService.getFeed…(page, perPage, memberId)");
        return feedPhotos;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<FeedPhotoWrapper.FeedPhotoListWrapper> getFeedPhotos(int i, int i2) {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<FeedPhotoWrapper.FeedPhotoListWrapper> feedPhotos = hornetApiService2.getFeedPhotos(i, i2);
        Intrinsics.checkExpressionValueIsNotNull(feedPhotos, "hornetApiService.getFeedPhotos(page, perPage)");
        return feedPhotos;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public void onResumeKernels() {
        getSessionKernel().onResume();
        getLookupKernel().onResume();
        getChatsInteractor().onResume(this.context);
        getWalletInteractor().onResume(this.context);
        getFilterKernel().onResume();
    }

    @Override // com.hornet.android.net.HornetApiClient
    public void onStartKernels() {
        onCreateKernels();
    }

    @Override // com.hornet.android.net.HornetApiClient
    public void onCreateKernels() {
        getSessionKernel().onCreate();
        getLookupKernel().onCreate();
        getFilterKernel().onCreate();
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Object createDeviceSignature(String str) {
        String string = Settings.Secure.getString(this.context.getContentResolver(), "android_id");
        Mac instance2 = Mac.getInstance("HmacSHA256");
        String string2 = this.context.getString(R.string.hornet_api_private_key);
        Intrinsics.checkExpressionValueIsNotNull(string2, "context\n\t\t\t\t\t\t.getString…g.hornet_api_private_key)");
        Charset charset = Charsets.US_ASCII;
        if (string2 != null) {
            byte[] bytes = string2.getBytes(charset);
            Intrinsics.checkExpressionValueIsNotNull(bytes, "(this as java.lang.String).getBytes(charset)");
            instance2.init(new SecretKeySpec(bytes, "HmacSHA256"));
            Intrinsics.checkExpressionValueIsNotNull(string, "udid");
            Charset charset2 = Charsets.US_ASCII;
            if (string != null) {
                byte[] bytes2 = string.getBytes(charset2);
                Intrinsics.checkExpressionValueIsNotNull(bytes2, "(this as java.lang.String).getBytes(charset)");
                byte[] doFinal = instance2.doFinal(bytes2);
                KeyUtil keyUtil = KeyUtil.INSTANCE;
                if (str == null) {
                    Intrinsics.throwNpe();
                }
                Context context2 = this.context;
                Intrinsics.checkExpressionValueIsNotNull(instance2, "hmac");
                KeyUtil.Special Special = keyUtil.Special(str, context2, instance2);
                if (Special != null) {
                    return Special;
                }
                Intrinsics.checkExpressionValueIsNotNull(doFinal, "signature");
                StringBuilder sb = new StringBuilder();
                for (byte b : doFinal) {
                    StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
                    Locale locale = Locale.US;
                    Intrinsics.checkExpressionValueIsNotNull(locale, "Locale.US");
                    Object[] objArr = {Byte.valueOf(b)};
                    String format = String.format(locale, "%02x", Arrays.copyOf(objArr, objArr.length));
                    Intrinsics.checkExpressionValueIsNotNull(format, "java.lang.String.format(locale, format, *args)");
                    sb.append(format);
                    Intrinsics.checkExpressionValueIsNotNull(sb, "buffer.append(String.for…Locale.US, \"%02x\", byte))");
                }
                String sb2 = sb.toString();
                Intrinsics.checkExpressionValueIsNotNull(sb2, "signature.fold(StringBui…x\", byte))\n\t\t}.toString()");
                return sb2;
            }
            throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
        }
        throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<HornetBadgeProgressResponse> getHornetBadgeProgress() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<HornetBadgeProgressResponse> hornetBadgeProgress = hornetApiService2.getHornetBadgeProgress();
        Intrinsics.checkExpressionValueIsNotNull(hornetBadgeProgress, "hornetApiService.hornetBadgeProgress");
        return hornetBadgeProgress;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<CampaignWrapper> getCampaign(String str) {
        Intrinsics.checkParameterIsNotNull(str, "id");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<CampaignWrapper> campaign = hornetApiService2.getCampaign(str);
        Intrinsics.checkExpressionValueIsNotNull(campaign, "hornetApiService.getCampaign(id)");
        return campaign;
    }

    private final KeyUtil.Special createSpecial() {
        FullMemberWrapper.FullMember profile;
        SessionData.Session session = getSessionKernel().getSession();
        Object createDeviceSignature = createDeviceSignature(String.valueOf((session == null || (profile = session.getProfile()) == null) ? null : profile.id));
        if (createDeviceSignature != null) {
            return (KeyUtil.Special) createDeviceSignature;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.hornet.android.utils.KeyUtil.Special");
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<SsoTokensResponse> getSsoTokens(String str, String str2) {
        Intrinsics.checkParameterIsNotNull(str, "destination");
        Intrinsics.checkParameterIsNotNull(str2, OstSdk.TOKEN);
        KeyUtil.Special createSpecial = createSpecial();
        SsoTokensRequest ssoTokensRequest = new SsoTokensRequest(createSpecial.getN(), createSpecial.getT(), createSpecial.getS());
        if (str.length() == 0) {
            HornetApiService hornetApiService2 = this.hornetApiService;
            if (hornetApiService2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
            }
            ssoTokensRequest.rt = str2;
            Single<SsoTokensResponse> ssoTokens = hornetApiService2.getSsoTokens(ssoTokensRequest, "exchange");
            Intrinsics.checkExpressionValueIsNotNull(ssoTokens, "hornetApiService.getSsoT…rt = token }, \"exchange\")");
            return ssoTokens;
        }
        HornetApiService hornetApiService3 = this.hornetApiService;
        if (hornetApiService3 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        ssoTokensRequest.destination = str;
        Single<SsoTokensResponse> ssoTokens2 = hornetApiService3.getSsoTokens(ssoTokensRequest, "");
        Intrinsics.checkExpressionValueIsNotNull(ssoTokens2, "hornetApiService.getSsoT…tion = destination }, \"\")");
        return ssoTokens2;
    }

    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0004\u0018\u00002\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0004H\u0002J\u0010\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0016¨\u0006\n"}, d2 = {"Lcom/hornet/android/net/HornetApiClientImpl$APIHeaderInterceptor;", "Lokhttp3/Interceptor;", "(Lcom/hornet/android/net/HornetApiClientImpl;)V", "encodeForHeaderValue", "", "rawValue", "intercept", "Lokhttp3/Response;", "chain", "Lokhttp3/Interceptor$Chain;", "app_productionRelease"}, k = 1, mv = {1, 1, 13})
    /* compiled from: HornetApiClientImpl.kt */
    public final class APIHeaderInterceptor implements Interceptor {
        /* JADX WARN: Incorrect args count in method signature: ()V */
        public APIHeaderInterceptor() {
        }

        /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0089: APUT  
          (r6v4 java.lang.Object[])
          (0 ??[int, short, byte, char])
          (wrap: java.lang.Double : 0x0085: INVOKE  (r7v3 java.lang.Double) = 
          (wrap: double : 0x0083: IGET  (r7v2 double) = (r7v1 com.google.android.gms.maps.model.LatLng) com.google.android.gms.maps.model.LatLng.latitude double)
         type: STATIC call: java.lang.Double.valueOf(double):java.lang.Double)
         */
        @Override // okhttp3.Interceptor
        public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
            Intrinsics.checkParameterIsNotNull(chain, "chain");
            Request.Builder newBuilder = chain.request().newBuilder();
            if (!TextUtils.isEmpty(HornetApiClientImpl.access$getPrefs$p(HornetApiClientImpl.this).accessToken().getOr(""))) {
                newBuilder.addHeader("Authorization", "Hornet " + HornetApiClientImpl.access$getPrefs$p(HornetApiClientImpl.this).accessToken().getOr(""));
            }
            Locale locale = Locale.getDefault();
            Intrinsics.checkExpressionValueIsNotNull(locale, "Locale.getDefault()");
            newBuilder.addHeader(HttpHeaders.ACCEPT_LANGUAGE, locale.getLanguage());
            newBuilder.addHeader("Accept", "application/json");
            if (HornetApiClientImpl.this.getLatlng() != null) {
                StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
                Locale locale2 = Locale.US;
                Intrinsics.checkExpressionValueIsNotNull(locale2, "Locale.US");
                Object[] objArr = new Object[2];
                LatLng latlng = HornetApiClientImpl.this.getLatlng();
                if (latlng == null) {
                    Intrinsics.throwNpe();
                }
                objArr[0] = Double.valueOf(latlng.latitude);
                LatLng latlng2 = HornetApiClientImpl.this.getLatlng();
                if (latlng2 == null) {
                    Intrinsics.throwNpe();
                }
                objArr[1] = Double.valueOf(latlng2.longitude);
                String format = String.format(locale2, "%f, %f", Arrays.copyOf(objArr, objArr.length));
                Intrinsics.checkExpressionValueIsNotNull(format, "java.lang.String.format(locale, format, *args)");
                newBuilder.addHeader("X-Device-Location", format);
            }
            newBuilder.addHeader("X-Device-Identifier", "" + Settings.Secure.getString(HornetApiClientImpl.this.context.getContentResolver(), "android_id"));
            StringCompanionObject stringCompanionObject2 = StringCompanionObject.INSTANCE;
            Object[] objArr2 = {BuildConfig.VERSION_NAME};
            String format2 = String.format("Android %s", Arrays.copyOf(objArr2, objArr2.length));
            Intrinsics.checkExpressionValueIsNotNull(format2, "java.lang.String.format(format, *args)");
            newBuilder.addHeader("X-Client-Version", format2);
            StringCompanionObject stringCompanionObject3 = StringCompanionObject.INSTANCE;
            String str = Build.MANUFACTURER;
            Intrinsics.checkExpressionValueIsNotNull(str, "Build.MANUFACTURER");
            String str2 = Build.MODEL;
            Intrinsics.checkExpressionValueIsNotNull(str2, "Build.MODEL");
            Object[] objArr3 = {encodeForHeaderValue(str), encodeForHeaderValue(str2)};
            String format3 = String.format("%s %s", Arrays.copyOf(objArr3, objArr3.length));
            Intrinsics.checkExpressionValueIsNotNull(format3, "java.lang.String.format(format, *args)");
            newBuilder.addHeader("X-Device-Name", format3);
            newBuilder.cacheControl(CacheControl.FORCE_NETWORK);
            okhttp3.Response proceed = chain.proceed(newBuilder.build());
            Intrinsics.checkExpressionValueIsNotNull(proceed, "chain.proceed(this.build())");
            return proceed;
        }

        private final String encodeForHeaderValue(String str) {
            String encode = Uri.encode(str, " ");
            Intrinsics.checkExpressionValueIsNotNull(encode, "Uri.encode(rawValue, \" \")");
            return encode;
        }
    }

    @Override // com.hornet.android.net.HornetApiClient
    public void activatePremium() {
        Companion.activatePremium();
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<HornetPointsResponse> getHornetPoints() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<HornetPointsResponse> hornetPoints = hornetApiService2.getHornetPoints();
        Intrinsics.checkExpressionValueIsNotNull(hornetPoints, "hornetApiService.hornetPoints");
        return hornetPoints;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<GetAwardsResponse> getAwards(String str) {
        Intrinsics.checkParameterIsNotNull(str, "activityId");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<GetAwardsResponse> awards = hornetApiService2.getAwards(str);
        Intrinsics.checkExpressionValueIsNotNull(awards, "hornetApiService.getAwards(activityId)");
        return awards;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Completable giveAwards(String str, int i) {
        Intrinsics.checkParameterIsNotNull(str, "activityId");
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Completable giveAward = hornetApiService2.giveAward(str, new GiveAwardRequest(i));
        Intrinsics.checkExpressionValueIsNotNull(giveAward, "hornetApiService.giveAwa…wardRequest(awardTypeId))");
        return giveAward;
    }

    @Override // com.hornet.android.net.HornetApiClient
    public Single<GetAwardsTypesResponse> getAwardsTypes() {
        HornetApiService hornetApiService2 = this.hornetApiService;
        if (hornetApiService2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hornetApiService");
        }
        Single<GetAwardsTypesResponse> awardsTypes = hornetApiService2.getAwardsTypes();
        Intrinsics.checkExpressionValueIsNotNull(awardsTypes, "hornetApiService.awardsTypes");
        return awardsTypes;
    }

    @Metadata(bv = {1, 0, 3}, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0006\u0010\u0007\u001a\u00020\bJ\u000e\u0010\t\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u000bR\u000e\u0010\u0003\u001a\u00020\u0004XT¢\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u000e¢\u0006\u0002\n\u0000¨\u0006\f"}, d2 = {"Lcom/hornet/android/net/HornetApiClientImpl$Companion;", "", "()V", "TAG", "", "instance", "Lcom/hornet/android/net/HornetApiClientImpl;", "activatePremium", "", "getInstance", "context", "Landroid/content/Context;", "app_productionRelease"}, k = 1, mv = {1, 1, 13})
    /* compiled from: HornetApiClientImpl.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final void activatePremium() {
            SessionKernel sessionKernel;
            SessionData.Session session;
            SessionData.Session.Account account;
            SessionData.Session.Account.Premium premium;
            HornetApiClientImpl hornetApiClientImpl = HornetApiClientImpl.instance;
            if (hornetApiClientImpl != null && (sessionKernel = hornetApiClientImpl.getSessionKernel()) != null && (session = sessionKernel.getSession()) != null && (account = session.getAccount()) != null && (premium = account.getPremium()) != null) {
                premium.activatePremium();
            }
        }

        public final HornetApiClientImpl getInstance(Context context) {
            HornetApiClientImpl hornetApiClientImpl;
            Intrinsics.checkParameterIsNotNull(context, "context");
            synchronized (this) {
                if (HornetApiClientImpl.instance == null) {
                    Context applicationContext = context.getApplicationContext();
                    Intrinsics.checkExpressionValueIsNotNull(applicationContext, "context.applicationContext");
                    WalletSessionValues walletSessionValues = null;
                    HornetApiClientImpl.instance = new HornetApiClientImpl(applicationContext, null);
                    HornetApiClientImpl hornetApiClientImpl2 = HornetApiClientImpl.instance;
                    if (hornetApiClientImpl2 == null) {
                        Intrinsics.throwNpe();
                    }
                    hornetApiClientImpl2.sessionKernel = SessionKernel.Companion.getInstance(context);
                    HornetApiClientImpl hornetApiClientImpl3 = HornetApiClientImpl.instance;
                    if (hornetApiClientImpl3 == null) {
                        Intrinsics.throwNpe();
                    }
                    hornetApiClientImpl3.filterKernel = FilterKernel.Companion.getInstance(context);
                    HornetApiClientImpl hornetApiClientImpl4 = HornetApiClientImpl.instance;
                    if (hornetApiClientImpl4 == null) {
                        Intrinsics.throwNpe();
                    }
                    hornetApiClientImpl4.lookupKernel = LookupKernel.Companion.getInstance(context);
                    HornetApiClientImpl hornetApiClientImpl5 = HornetApiClientImpl.instance;
                    if (hornetApiClientImpl5 == null) {
                        Intrinsics.throwNpe();
                    }
                    hornetApiClientImpl5.mqttKernel = MqttKernel.Companion.getInstance(context);
                    HornetApiClientImpl hornetApiClientImpl6 = HornetApiClientImpl.instance;
                    if (hornetApiClientImpl6 == null) {
                        Intrinsics.throwNpe();
                    }
                    hornetApiClientImpl6.entitlementKernel = EntitlementKernel.Companion.getInstance(context);
                    HornetApiClientImpl hornetApiClientImpl7 = HornetApiClientImpl.instance;
                    if (hornetApiClientImpl7 == null) {
                        Intrinsics.throwNpe();
                    }
                    hornetApiClientImpl7.chatsInteractor = ChatsInteractor.Companion.getInstance(context);
                    HornetApiClientImpl hornetApiClientImpl8 = HornetApiClientImpl.instance;
                    if (hornetApiClientImpl8 == null) {
                        Intrinsics.throwNpe();
                    }
                    hornetApiClientImpl8.productInteractor = ProductInteractor.Companion.getInstance(context);
                    HornetApiClientImpl hornetApiClientImpl9 = HornetApiClientImpl.instance;
                    if (hornetApiClientImpl9 == null) {
                        Intrinsics.throwNpe();
                    }
                    WalletInteractor.Companion companion = WalletInteractor.Companion;
                    HornetApiClientImpl hornetApiClientImpl10 = HornetApiClientImpl.instance;
                    if (hornetApiClientImpl10 == null) {
                        Intrinsics.throwNpe();
                    }
                    SessionData.Session session = hornetApiClientImpl10.getSessionKernel().getSession();
                    if (session != null) {
                        walletSessionValues = session.getWalletSessionValues();
                    }
                    hornetApiClientImpl9.walletInteractor = companion.getInstance(context, walletSessionValues);
                    HornetApiClientImpl hornetApiClientImpl11 = HornetApiClientImpl.instance;
                    if (hornetApiClientImpl11 == null) {
                        Intrinsics.throwNpe();
                    }
                    hornetApiClientImpl11.afterInject$app_productionRelease();
                }
                hornetApiClientImpl = HornetApiClientImpl.instance;
                if (hornetApiClientImpl == null) {
                    Intrinsics.throwNpe();
                }
            }
            return hornetApiClientImpl;
        }
    }
}
