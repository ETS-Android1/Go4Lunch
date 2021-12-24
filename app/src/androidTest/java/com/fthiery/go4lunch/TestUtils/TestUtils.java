package com.fthiery.go4lunch.TestUtils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kotlin.jvm.internal.Intrinsics;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.BufferedSource;
import okio.Okio;

public class TestUtils {

    public static <T> T getOrAwaitValue(final LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(@Nullable T o) {
                data[0] = o;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        liveData.observeForever(observer);
        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw new RuntimeException("LiveData value was never set.");
        }
        //noinspection unchecked
        return (T) data[0];
    }

    public static FirebaseFirestore firebaseEmulatorInstance() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        try {
            firestore.useEmulator("10.0.2.2", 8080);

            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .build();
            firestore.setFirestoreSettings(settings);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return firestore;
    }

    public static void clearFirebase() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/emulator/v1/projects/go4lunch-56d5b/databases/(default)/documents")
                .delete()
                .build();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i("Firebase Clear", "Failed to connect: " + e.getMessage());
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.i("Firebase Clear", "Success: " + response.code());
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static final String nearbySearchResponse = "{\n" +
            "   \"html_attributions\" : [],\n" +
            "   \"next_page_token\" : \"Aap_uEDpsqlffL0TYMzjz-j-5Rt7WQNRPX9YjfGDC5WOrSuBQebI9SXf3810ACW6zjMPc2Eq14770ae7zQTxO9Am0pAmmGUMPODAGgCrDVgQ7Su2_vJQdiVrWWR0bnej9EIBYzzUG99MEkRNj_aZtbGWRJCVE5mqsgI-OANmleQNG0P4XT5gAWahwKjQs6BOScGCHvdyNmVaIJQAp6_eNqmJUXyDa03byETEVnnt57bp-cLcZLVlG0EFQRUNsmz4qj9OuZzP6nk-z1fuM8IBVkitcJ58yoi1hklA5X1vBLpcmNDpz2ZfakhGy-8jDN34ZDxv1ycpfQuc0C210tGoRC6yKdhZUn6baWC7r7ie0nP0Me9w9p1kJBi9sdwDvlfyYXMv5fqFS2oljxz5XFX2p_Ldz7A8RvCpuXHbL4YkNr8NZrswCn7OdcTp1C3jtw_O\",\n" +
            "   \"results\" : [\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.8703417,\n" +
            "               \"lng\" : 151.1979222\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.86900816970849,\n" +
            "                  \"lng\" : 151.1991662802915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.87170613029149,\n" +
            "                  \"lng\" : 151.1964683197085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/restaurant-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/restaurant_pinlet\",\n" +
            "         \"name\" : \"The Little Snail Restaurant\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : false\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 900,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/114727320476039103791\\\"\\u003eThe Little Snail Restaurant\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uEABtBBorDC3L-sRVHyuQrMDl-r-6wTrcOO0omK9k2DHMYsb91lzwXE9_s906jSSVZUus13GiYVKh5uAd_4NVHQqJVvK9R-606OpTC1CKNn0d2qYHb9VYXeYi1dqwTufMorpvOQT8l6lfJcEbV-TAFnR8hYVESkh4k6pxf_5XPXIZpct\",\n" +
            "               \"width\" : 1350\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJtwapWjeuEmsRcxV5JARHpSk\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"45HX+V5 Pyrmont Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH45HX+V5\"\n" +
            "         },\n" +
            "         \"price_level\" : 2,\n" +
            "         \"rating\" : 4.5,\n" +
            "         \"reference\" : \"ChIJtwapWjeuEmsRcxV5JARHpSk\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [ \"restaurant\", \"food\", \"point_of_interest\", \"establishment\" ],\n" +
            "         \"user_ratings_total\" : 1764,\n" +
            "         \"vicinity\" : \"3/50 Murray Street, Pyrmont\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.8666848,\n" +
            "               \"lng\" : 151.2015819\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.86532001970851,\n" +
            "                  \"lng\" : 151.2030454802915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.8680179802915,\n" +
            "                  \"lng\" : 151.2003475197085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/restaurant-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/restaurant_pinlet\",\n" +
            "         \"name\" : \"Steersons Steakhouse\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : false\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 1538,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/110384774118527817485\\\"\\u003eSteersons Steakhouse\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uEAY5u7sIoMpQAiB5VUeCHtPpdt1q4NgWKZ_PxTC1sUuBK5yHH2o1Sp-u2Vawx2LFu74ypU4jcQba7MphbRlPjIA7JcjiQ_ieQRysxrlElzwbTQhTWUQtzpXnYLcq4a32_dEIBt7zCaIOTRSQjNiczUaTUStyrh_Y5qO9I_lsPKCPAY2\",\n" +
            "               \"width\" : 2048\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJWRxefkeuEmsRn2tDqHg6HQo\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"46M2+8J Cité de Sydney Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH46M2+8J\"\n" +
            "         },\n" +
            "         \"price_level\" : 3,\n" +
            "         \"rating\" : 4.3,\n" +
            "         \"reference\" : \"ChIJWRxefkeuEmsRn2tDqHg6HQo\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [ \"restaurant\", \"food\", \"point_of_interest\", \"establishment\" ],\n" +
            "         \"user_ratings_total\" : 587,\n" +
            "         \"vicinity\" : \"17 Lime Street, Sydney\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.866664,\n" +
            "               \"lng\" : 151.2014112\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.8653004697085,\n" +
            "                  \"lng\" : 151.2029594302915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.8679984302915,\n" +
            "                  \"lng\" : 151.2002614697085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/bar-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/bar_pinlet\",\n" +
            "         \"name\" : \"Georges Mediterranean Bar & Grill\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : false\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 829,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/109724308524689410874\\\"\\u003eGeorges Mediterranean Bar &amp; Grill\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uEANh7TZyzOGea1taB7EmbwN3Q_bgm4G2mSvVgWgB--UtCVx__-qTVMKcQGCkc7CRguOM4ZPcN4xLVu_P3N8qF-mPFkjlsK9FYru9CAGtGtxSebi5U_udgrwfrSH38415r8OR2x6rCWzlVGJdzguCgPlSeOw4WfyU-ogPtlVavkrB1JR\",\n" +
            "               \"width\" : 1244\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJWRxefkeuEmsRmKvNb_DeLt8\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"46M2+8H Cité de Sydney Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH46M2+8H\"\n" +
            "         },\n" +
            "         \"price_level\" : 2,\n" +
            "         \"rating\" : 3.8,\n" +
            "         \"reference\" : \"ChIJWRxefkeuEmsRmKvNb_DeLt8\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [ \"bar\", \"restaurant\", \"food\", \"point_of_interest\", \"establishment\" ],\n" +
            "         \"user_ratings_total\" : 577,\n" +
            "         \"vicinity\" : \"King Street Wharf, 3 The Promenade, Sydney\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.8683472,\n" +
            "               \"lng\" : 151.1962233\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.8669304197085,\n" +
            "                  \"lng\" : 151.1976596802915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.86962838029149,\n" +
            "                  \"lng\" : 151.1949617197085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/restaurant-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/restaurant_pinlet\",\n" +
            "         \"name\" : \"Flying Fish\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : false\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 3907,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/117229116771790632690\\\"\\u003eFlying Fish\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uECOaY7xoY03M8scRVtOy8_zsspF78EJ5ri0mw11ITdh3m-mNtoR812H4-2RoVCA6-kvYFU23czCF9pZJnYWF95mA7j57v3uqkekC58-a99K0jg8L5d9csbeCTPBa6Ui_ncvALL0pbS-Vg8DmXYgpMMwXZCAbXGUL_ue-V3dmDwtMsyE\",\n" +
            "               \"width\" : 5373\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJm7Ex8UmuEmsR37p4Hm0D0VI\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"45JW+MF Pyrmont Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH45JW+MF\"\n" +
            "         },\n" +
            "         \"price_level\" : 1,\n" +
            "         \"rating\" : 4.4,\n" +
            "         \"reference\" : \"ChIJm7Ex8UmuEmsR37p4Hm0D0VI\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [ \"bar\", \"restaurant\", \"food\", \"point_of_interest\", \"establishment\" ],\n" +
            "         \"user_ratings_total\" : 948,\n" +
            "         \"vicinity\" : \"80 Pyrmont Street, Pyrmont\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.867806,\n" +
            "               \"lng\" : 151.2017201\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.8664656697085,\n" +
            "                  \"lng\" : 151.2031799802915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.8691636302915,\n" +
            "                  \"lng\" : 151.2004820197085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/restaurant-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/restaurant_pinlet\",\n" +
            "         \"name\" : \"The Malaya\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : false\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 885,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/112797482079521405927\\\"\\u003eThe Malaya\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uEAQ-E7ZtfF6YmRhFVZN81mVonzZurNBkvgAeuOYa5vIReLkJ-RKWC3eapCbEOyq1tlnmfxQidOADy3yAUQoMQRxXIIkH_9B3P2INJPE9sUJ6UKvEAlgqlN6zQEj84KpYBDUOGzcHjOGMVrXqhtCogij_mIF8rFAnBhS9HaC38FmP6Rs\",\n" +
            "               \"width\" : 884\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJ4U8HhjiuEmsRyevJVTxWbFo\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"46J2+VM Cité de Sydney Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH46J2+VM\"\n" +
            "         },\n" +
            "         \"price_level\" : 3,\n" +
            "         \"rating\" : 4.5,\n" +
            "         \"reference\" : \"ChIJ4U8HhjiuEmsRyevJVTxWbFo\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [ \"restaurant\", \"food\", \"point_of_interest\", \"establishment\" ],\n" +
            "         \"user_ratings_total\" : 1284,\n" +
            "         \"vicinity\" : \"39 Lime Street, Sydney\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.8712696,\n" +
            "               \"lng\" : 151.1990764\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.86954436970851,\n" +
            "                  \"lng\" : 151.2000878802915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.87224233029151,\n" +
            "                  \"lng\" : 151.1973899197085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/restaurant-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/restaurant_pinlet\",\n" +
            "         \"name\" : \"Blue Fish Sydney Seafood Restaurant\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : false\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 3000,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/106312104891296782110\\\"\\u003eJohn Lawson\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uED7Oya1iMNoJKvP7FcEBK_V7zBW4Uqwd1gxeHn9wrOIl5j3sp9QnxA9BnRABMeS3XDHqMndC46ziPlrpTb3Oqst4ohL5UO4fKo3prBOnlo_iOb2AM6qXYx3Swc2iqHA1Q_GcQ2S9HSvmEyhEhFuAO5EK3sCFhZUAkbje6KJvJw4Oh2F\",\n" +
            "               \"width\" : 4000\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJBaqDDzquEmsRcCI5e-3rStM\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"45HX+FJ Cité de Sydney Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH45HX+FJ\"\n" +
            "         },\n" +
            "         \"price_level\" : 3,\n" +
            "         \"rating\" : 3.8,\n" +
            "         \"reference\" : \"ChIJBaqDDzquEmsRcCI5e-3rStM\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [ \"restaurant\", \"food\", \"point_of_interest\", \"establishment\" ],\n" +
            "         \"user_ratings_total\" : 931,\n" +
            "         \"vicinity\" : \"Shop 287 Harbourside Shopping Centre Darling Harbour, Sydney\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.86756700000001,\n" +
            "               \"lng\" : 151.193742\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.8661819197085,\n" +
            "                  \"lng\" : 151.1951371302915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.8688798802915,\n" +
            "                  \"lng\" : 151.1924391697085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/restaurant-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/restaurant_pinlet\",\n" +
            "         \"name\" : \"Blue Eye Dragon\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : false\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 2723,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/114550096924148386356\\\"\\u003eBlue Eye Dragon\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uEBijemOA0hEGs__mBv90I6HLSJoQbT23WJWXrKX9OyVW2ly9SWH2tymk-q4OdwLJCi954W9CiCmV7lPw1XeRjBuHo8BEBKy9zkZQusepjGxTHtOg8olUI1w3BZxVbHTv4iU96mbL7eMQsoXSIX6duQBkbk_63vYRpZX85iiZP3-G525\",\n" +
            "               \"width\" : 3776\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJuZqIiTauEmsRJF_TK9Vpfmw\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"45JV+XF Pyrmont Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH45JV+XF\"\n" +
            "         },\n" +
            "         \"price_level\" : 2,\n" +
            "         \"rating\" : 4.5,\n" +
            "         \"reference\" : \"ChIJuZqIiTauEmsRJF_TK9Vpfmw\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [ \"restaurant\", \"food\", \"point_of_interest\", \"establishment\" ],\n" +
            "         \"user_ratings_total\" : 274,\n" +
            "         \"vicinity\" : \"37 Pyrmont Street, Pyrmont\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.8707409,\n" +
            "               \"lng\" : 151.1989615\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.8694323697085,\n" +
            "                  \"lng\" : 151.2000751302915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.8721303302915,\n" +
            "                  \"lng\" : 151.1973771697085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/restaurant-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/restaurant_pinlet\",\n" +
            "         \"name\" : \"Hurricane's Grill Darling Harbour\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : false\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 533,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/103760746154304436690\\\"\\u003eHurricane&#39;s Grill Darling Harbour\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uEBhFan23LkeMxavyVSTTZCuk30bsXkD460t9kM7eMtSEh3F0nYvAZQffQ81REP2021Uh_QvvktR6M_dJCw-HHQr2qhljr3akJ6CKJvf8P1UFw0wCYkVQ19rwDCw_8IEJVyF5N9YEera0ffpFdROUUjKIs8fNl0nCbnvTHSADMA5eKJR\",\n" +
            "               \"width\" : 800\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJL7SCpzCuEmsRN8MD9vPXmOs\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"45HX+PH Cité de Sydney Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH45HX+PH\"\n" +
            "         },\n" +
            "         \"price_level\" : 2,\n" +
            "         \"rating\" : 3.9,\n" +
            "         \"reference\" : \"ChIJL7SCpzCuEmsRN8MD9vPXmOs\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [ \"bar\", \"restaurant\", \"food\", \"point_of_interest\", \"establishment\" ],\n" +
            "         \"user_ratings_total\" : 1989,\n" +
            "         \"vicinity\" : \"Shops 433 - 436 Darling Drive, Sydney\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.8713517,\n" +
            "               \"lng\" : 151.1947299\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.8699455197085,\n" +
            "                  \"lng\" : 151.1961329802915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.8726434802915,\n" +
            "                  \"lng\" : 151.1934350197085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/lodging-71.png\",\n" +
            "         \"icon_background_color\" : \"#909CE1\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/hotel_pinlet\",\n" +
            "         \"name\" : \"Dunkirk Hotel\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : true\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 3180,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/100619007458021702675\\\"\\u003eDunkirk Hotel\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uEBO99Dq-fpqtgYGhQvoKzzM0dSaBzn6ihnU9qaPbFkwQUlJHfxCCoCE5f_zLWesFB90STDhA_9RIiEczeBpAN4Hpk6CoZ7vndgmCFuB9DqEJiuKwijNWC5oUDkCzyeMrCWBr789RzfrH8nzgYLWtRyiX2nqzQSpgI4zfWa6DxhUpeOU\",\n" +
            "               \"width\" : 4770\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJ4UEnPTGuEmsRkjpDW7FRSko\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"45HV+FV Pyrmont Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH45HV+FV\"\n" +
            "         },\n" +
            "         \"rating\" : 4.3,\n" +
            "         \"reference\" : \"ChIJ4UEnPTGuEmsRkjpDW7FRSko\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [\n" +
            "            \"lodging\",\n" +
            "            \"bar\",\n" +
            "            \"liquor_store\",\n" +
            "            \"restaurant\",\n" +
            "            \"food\",\n" +
            "            \"point_of_interest\",\n" +
            "            \"store\",\n" +
            "            \"establishment\"\n" +
            "         ],\n" +
            "         \"user_ratings_total\" : 331,\n" +
            "         \"vicinity\" : \"205 Harris Street, Pyrmont\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.8636013,\n" +
            "               \"lng\" : 151.1944983\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.8626499197085,\n" +
            "                  \"lng\" : 151.1953526302915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.8653478802915,\n" +
            "                  \"lng\" : 151.1926546697085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/generic_business-71.png\",\n" +
            "         \"icon_background_color\" : \"#7B9EB0\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/generic_pinlet\",\n" +
            "         \"name\" : \"Cafe Morso\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : false\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 534,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/115976805574107278435\\\"\\u003eCafe Morso\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uECGVmgcrWaYqi3Bj_G-1xtjfnb5oi4KtEOeda5Vvk26eiF3Tb3xZ9CynDllE7BUxwQaThXmfv3B3ojmvCflT3_QfefqLI8AIhTzPEHw2TF7NCGnl-b373HsDWvVnHI4WUwuA3YKifqRU6BuVeKP3pSZbByYG82mW_t6fZnNu64NvETX\",\n" +
            "               \"width\" : 800\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJz2EHuEmuEmsRN_yScfn88Ec\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"45PV+HQ Pyrmont Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH45PV+HQ\"\n" +
            "         },\n" +
            "         \"price_level\" : 2,\n" +
            "         \"rating\" : 4.3,\n" +
            "         \"reference\" : \"ChIJz2EHuEmuEmsRN_yScfn88Ec\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [\n" +
            "            \"cafe\",\n" +
            "            \"restaurant\",\n" +
            "            \"food\",\n" +
            "            \"point_of_interest\",\n" +
            "            \"store\",\n" +
            "            \"establishment\"\n" +
            "         ],\n" +
            "         \"user_ratings_total\" : 359,\n" +
            "         \"vicinity\" : \"108/26-32 Pirrama Road, Pyrmont\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.8659992,\n" +
            "               \"lng\" : 151.2015769\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.86461146970851,\n" +
            "                  \"lng\" : 151.2030046302915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.86730943029151,\n" +
            "                  \"lng\" : 151.2003066697085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/bar-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/bar_pinlet\",\n" +
            "         \"name\" : \"The Loft\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : true\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 1364,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/105403356651468226795\\\"\\u003eThe Loft\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uEDgAouUqiz5WsmHjK7xroR2wV_CdN1uX-iicIh3ARSKKG2UNSVaK4gy6__NevtyXnFQb87tRh1TT02E1JUbKR_eRwF8sZYJOR5m0LYuPUXABdbO34v82AOxX3az3-GFtBWrDK_V1RVt6-zum2dU3pn8Wr4bwLjCY1keXIXFkvvKX8vr\",\n" +
            "               \"width\" : 2046\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJxZ3vgTiuEmsRjsB0BmHCaiw\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"46M2+JJ Cité de Sydney Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH46M2+JJ\"\n" +
            "         },\n" +
            "         \"price_level\" : 2,\n" +
            "         \"rating\" : 4.2,\n" +
            "         \"reference\" : \"ChIJxZ3vgTiuEmsRjsB0BmHCaiw\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [ \"bar\", \"restaurant\", \"food\", \"point_of_interest\", \"establishment\" ],\n" +
            "         \"user_ratings_total\" : 232,\n" +
            "         \"vicinity\" : \"3 Lime Street, Sydney\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.8721564,\n" +
            "               \"lng\" : 151.199033\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.8710052697085,\n" +
            "                  \"lng\" : 151.2003346302915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.8737032302915,\n" +
            "                  \"lng\" : 151.1976366697085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/restaurant-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/restaurant_pinlet\",\n" +
            "         \"name\" : \"Zaaffran\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : false\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 753,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/109093090202583933945\\\"\\u003eGaijin\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uEA9vnLq1EQw01Idxji2O4fQstSktxzuGdjGqFtgm5O1LaD1szBgO2C_VrZ5NJg_2xf2uBQgmwTjY-ql2Z8Ub-X8w9u71VH1oc8qr_EQpkxTiw9etwafyM-3FMbQRWpzbMc_8SXW3UPpnTOPCbHm6amAxZUXpaTV8MzdQFeUoPVH_VJT\",\n" +
            "               \"width\" : 1005\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJL7SCpzCuEmsRS8JWUImDVsM\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"45HX+4J Cité de Sydney Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH45HX+4J\"\n" +
            "         },\n" +
            "         \"price_level\" : 3,\n" +
            "         \"rating\" : 4,\n" +
            "         \"reference\" : \"ChIJL7SCpzCuEmsRS8JWUImDVsM\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [ \"restaurant\", \"food\", \"point_of_interest\", \"establishment\" ],\n" +
            "         \"user_ratings_total\" : 861,\n" +
            "         \"vicinity\" : \"Harbourside Shopping Centre, 345/10 Darling Drive, Sydney\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.8672213,\n" +
            "               \"lng\" : 151.201675\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.8659826697085,\n" +
            "                  \"lng\" : 151.2029768802915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.86868063029149,\n" +
            "                  \"lng\" : 151.2002789197085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/bar-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/bar_pinlet\",\n" +
            "         \"name\" : \"The Sporting Globe x 4 Pines\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : true\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 853,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/109194350477101623901\\\"\\u003eThe Sporting Globe x 4 Pines\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uECyS_93tWU512mJsHtgdRAMtge2HXwKxElcomMm2eb41zI8DbqNmgeNUvW2FTt-hZ1fFDcA0E3njKDy4DA5y3_cZcNJ5ym8lsb36XXvIFo2rGOWh9AzRp7lBsuqwvIwFbZ1oSxx0t6sQrwPXfwSnj3AtTJWQO7ROr2KR64xM4jIEjru\",\n" +
            "               \"width\" : 1280\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJubgBeUeuEmsRiUUSL_SiZsw\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"46M2+4M Cité de Sydney Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH46M2+4M\"\n" +
            "         },\n" +
            "         \"price_level\" : 2,\n" +
            "         \"rating\" : 4.2,\n" +
            "         \"reference\" : \"ChIJubgBeUeuEmsRiUUSL_SiZsw\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [ \"bar\", \"restaurant\", \"food\", \"point_of_interest\", \"establishment\" ],\n" +
            "         \"user_ratings_total\" : 2223,\n" +
            "         \"vicinity\" : \"King Street Wharf, 22 The Promenade, Sydney\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.8719889,\n" +
            "               \"lng\" : 151.1991111\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.87093996970851,\n" +
            "                  \"lng\" : 151.2003202802915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.8736379302915,\n" +
            "                  \"lng\" : 151.1976223197085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/restaurant-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/restaurant_pinlet\",\n" +
            "         \"name\" : \"Criniti's Darling Harbour\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : true\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 4000,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/102877242281974526521\\\"\\u003eCriniti&#39;s Darling Harbour\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uEBjPYcfac2-TGxTeKRqygjw_ieB_Acu1NGXWuMVMq7xeH2mMNikqR9Gn2zE9lVp3GY4fwWI5ervWPTpCoyTd0opgApMbcT96scYoOlwIFwpeqH8W23uqkKtMbIvapbJgnQlcY81uvY1mBaLvsPowhwO-eFQKI0tpbNg_9CFStHyAXaL\",\n" +
            "               \"width\" : 6000\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJL7SCpzCuEmsRTt1uJsaxMBQ\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"45HX+6J Cité de Sydney Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH45HX+6J\"\n" +
            "         },\n" +
            "         \"price_level\" : 2,\n" +
            "         \"rating\" : 3.2,\n" +
            "         \"reference\" : \"ChIJL7SCpzCuEmsRTt1uJsaxMBQ\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [ \"restaurant\", \"food\", \"point_of_interest\", \"establishment\" ],\n" +
            "         \"user_ratings_total\" : 2107,\n" +
            "         \"vicinity\" : \"Level 2 Harbourside Shopping Centre 2, 10 Darling Drive, Sydney\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.8666424,\n" +
            "               \"lng\" : 151.2036552\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.86534376970851,\n" +
            "                  \"lng\" : 151.2050183302915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.86804173029151,\n" +
            "                  \"lng\" : 151.2023203697085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/bar-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/bar_pinlet\",\n" +
            "         \"name\" : \"The Captains Balcony\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : true\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 423,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/100392152225838948305\\\"\\u003eThe Captains Balcony\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uEAI4bcbnhtgRjdVFL2py3fk5jU_CVaLYRj1YKdsalUebTc1EbVOE9Pz_c7MBHKf_w9zszK7L_fEDabsOF3KI_9ZAADxcGftlG7vb_zgPpHf75Zfhdzkswq5HCt6ho9-TFWwy2MVPTcII7Lm8fBED5_n6rHrwh-0671Is2zbKc59k-Ru\",\n" +
            "               \"width\" : 750\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJ2eAQUEeuEmsRcLznK5EjcsA\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"46M3+8F Cité de Sydney Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH46M3+8F\"\n" +
            "         },\n" +
            "         \"price_level\" : 2,\n" +
            "         \"rating\" : 4.5,\n" +
            "         \"reference\" : \"ChIJ2eAQUEeuEmsRcLznK5EjcsA\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [\n" +
            "            \"bar\",\n" +
            "            \"night_club\",\n" +
            "            \"restaurant\",\n" +
            "            \"food\",\n" +
            "            \"point_of_interest\",\n" +
            "            \"establishment\"\n" +
            "         ],\n" +
            "         \"user_ratings_total\" : 524,\n" +
            "         \"vicinity\" : \"46 Erskine Street, Sydney\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.87135119999999,\n" +
            "               \"lng\" : 151.198827\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.8705162,\n" +
            "                  \"lng\" : 151.2003470302915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.8737118,\n" +
            "                  \"lng\" : 151.1976490697085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/restaurant-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/restaurant_pinlet\",\n" +
            "         \"name\" : \"Hard Rock Cafe\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : false\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 4389,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/101107416627900781357\\\"\\u003eHard Rock Cafe\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uEBg-IuItT08kpJFGUVUqZsLDOt1KhoqwbNa0MBqMT81z8avRHMoTJDN5Aj5RXxGkbwlDgswiWpmADjaRSgPvj_wrjnY0pAzsiln9Dkq0_ZtCj_bDf2XYgPvXvOUfqZnRYJsCFHZ2FGzOfSxezVx-Yw_SZp-rWMs9I-PchmdQwcsPak\",\n" +
            "               \"width\" : 6583\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJC5popzCuEmsRPKATD1RBAAQ\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"45HX+FG Cité de Sydney Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH45HX+FG\"\n" +
            "         },\n" +
            "         \"price_level\" : 2,\n" +
            "         \"rating\" : 3.9,\n" +
            "         \"reference\" : \"ChIJC5popzCuEmsRPKATD1RBAAQ\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [ \"restaurant\", \"food\", \"point_of_interest\", \"establishment\" ],\n" +
            "         \"user_ratings_total\" : 2280,\n" +
            "         \"vicinity\" : \"2-10 Darling Dr, Harbourside Shopping Centre, Darling Harbour, Sydney\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.8661449,\n" +
            "               \"lng\" : 151.2013767\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.8646829697085,\n" +
            "                  \"lng\" : 151.2029044302915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.8673809302915,\n" +
            "                  \"lng\" : 151.2002064697085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/bar-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/bar_pinlet\",\n" +
            "         \"name\" : \"Bungalow 8\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : true\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 1365,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/115360168180972631051\\\"\\u003eBungalow 8\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uECf2kRsVadhkEo1cguozwJfgPp99kA72JFAzDZbMZUxa-aBZTt9LDpKu1trTX9PJWd-8wl2o6h6eUdiCgZYlvBaZFTTNtRyfHkYdKiZxCDUeUagDNNUS_oHgQTrk1frf-Df2YrbTuM9rJfwW502-U2n2ZS4lo5OwSobH15bUOhH1h1i\",\n" +
            "               \"width\" : 2048\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJOXAGd0euEmsRuzx3RATwLHY\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"46M2+GH Cité de Sydney Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH46M2+GH\"\n" +
            "         },\n" +
            "         \"price_level\" : 2,\n" +
            "         \"rating\" : 4,\n" +
            "         \"reference\" : \"ChIJOXAGd0euEmsRuzx3RATwLHY\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [\n" +
            "            \"bar\",\n" +
            "            \"night_club\",\n" +
            "            \"restaurant\",\n" +
            "            \"food\",\n" +
            "            \"point_of_interest\",\n" +
            "            \"establishment\"\n" +
            "         ],\n" +
            "         \"user_ratings_total\" : 2222,\n" +
            "         \"vicinity\" : \"3 Lime Street, Sydney\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.8668159,\n" +
            "               \"lng\" : 151.1973855\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.8655564697085,\n" +
            "                  \"lng\" : 151.1990422302915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.8682544302915,\n" +
            "                  \"lng\" : 151.1963442697085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/restaurant-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/restaurant_pinlet\",\n" +
            "         \"name\" : \"LuMi Dining\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : false\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 3024,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/107534161672426292836\\\"\\u003eSangchan Yoo\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uEDC8jdznqUzNmb-UiOoiMFQXQUQ0qTF6ZZyvz4uRNV4WXn9fQqlcLUiK8OO08tAj_ix6zZ6MBnfQPsc-nsC2ET30YvALxKvtvlrf6w14Kx345jE4a9MuEUQUZ9ytrMtvyKWC42r97HUXou95DHhuSTDZ1Rp9NoBLHb6LtmupUWbDmEH\",\n" +
            "               \"width\" : 4032\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJDZzo5DeuEmsRsi1wzrIp6HY\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"45MW+7X Pyrmont Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH45MW+7X\"\n" +
            "         },\n" +
            "         \"price_level\" : 4,\n" +
            "         \"rating\" : 4.6,\n" +
            "         \"reference\" : \"ChIJDZzo5DeuEmsRsi1wzrIp6HY\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [ \"bar\", \"restaurant\", \"food\", \"point_of_interest\", \"establishment\" ],\n" +
            "         \"user_ratings_total\" : 605,\n" +
            "         \"vicinity\" : \"56 Pirrama Road, Pyrmont\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.868232,\n" +
            "               \"lng\" : 151.201658\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.8665148697085,\n" +
            "                  \"lng\" : 151.2030190802915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.8692128302915,\n" +
            "                  \"lng\" : 151.2003211197085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/restaurant-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/restaurant_pinlet\",\n" +
            "         \"name\" : \"Casa Ristorante Italiano\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : false\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 704,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/110295778992735201361\\\"\\u003eCasa Ristorante Italiano\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uEC5y2WfH2SqYWoT43snJc0Kb0_3090jWPp2c6pV32-JENccyjx8uo5Cplgs-drwLYp8mEMqu1Y7pInlB0vMpolRNeOSPp1b1rrHYkK3A9jmAXPx7C0FDb-99O3_IrgCRbAeZIIMLiovK9iJZtdnxwh3RY54cD-XKWcjm23184fA-0dh\",\n" +
            "               \"width\" : 1016\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJL7gb_z-uEmsRCL4jbocltz0\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"46J2+PM Cité de Sydney Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH46J2+PM\"\n" +
            "         },\n" +
            "         \"price_level\" : 2,\n" +
            "         \"rating\" : 3.6,\n" +
            "         \"reference\" : \"ChIJL7gb_z-uEmsRCL4jbocltz0\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [ \"bar\", \"restaurant\", \"food\", \"point_of_interest\", \"establishment\" ],\n" +
            "         \"user_ratings_total\" : 1596,\n" +
            "         \"vicinity\" : \"42/48 The Promenade, Sydney\"\n" +
            "      },\n" +
            "      {\n" +
            "         \"business_status\" : \"OPERATIONAL\",\n" +
            "         \"geometry\" : {\n" +
            "            \"location\" : {\n" +
            "               \"lat\" : -33.8722434,\n" +
            "               \"lng\" : 151.199211\n" +
            "            },\n" +
            "            \"viewport\" : {\n" +
            "               \"northeast\" : {\n" +
            "                  \"lat\" : -33.8710269197085,\n" +
            "                  \"lng\" : 151.2003172302915\n" +
            "               },\n" +
            "               \"southwest\" : {\n" +
            "                  \"lat\" : -33.8737248802915,\n" +
            "                  \"lng\" : 151.1976192697085\n" +
            "               }\n" +
            "            }\n" +
            "         },\n" +
            "         \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/bar-71.png\",\n" +
            "         \"icon_background_color\" : \"#FF9E67\",\n" +
            "         \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/bar_pinlet\",\n" +
            "         \"name\" : \"The Watershed Hotel\",\n" +
            "         \"opening_hours\" : {\n" +
            "            \"open_now\" : true\n" +
            "         },\n" +
            "         \"photos\" : [\n" +
            "            {\n" +
            "               \"height\" : 1365,\n" +
            "               \"html_attributions\" : [\n" +
            "                  \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/116381090386619519743\\\"\\u003eThe Watershed Hotel\\u003c/a\\u003e\"\n" +
            "               ],\n" +
            "               \"photo_reference\" : \"Aap_uEA-6s_9C6Va_EMckYo_vuTsc8AXgT0-Y0uplA2HYsLx68D8HhNz0Aj51Htu4gHKz6oEvP2YwNReeBulCp5u-_OKqfjaAHs2BwN8TdeVltFUVOYy1A3KEiDCEaQUCzxGh_ddVhRsZiLlTwSxS9opQRG4PaP8L0TVqjwnx0mUGMc9Fb7P\",\n" +
            "               \"width\" : 2048\n" +
            "            }\n" +
            "         ],\n" +
            "         \"place_id\" : \"ChIJL7SCpzCuEmsR69oq3pM9AUU\",\n" +
            "         \"plus_code\" : {\n" +
            "            \"compound_code\" : \"45HX+4M Cité de Sydney Nouvelle-Galles du Sud, Australie\",\n" +
            "            \"global_code\" : \"4RRH45HX+4M\"\n" +
            "         },\n" +
            "         \"price_level\" : 2,\n" +
            "         \"rating\" : 3.8,\n" +
            "         \"reference\" : \"ChIJL7SCpzCuEmsR69oq3pM9AUU\",\n" +
            "         \"scope\" : \"GOOGLE\",\n" +
            "         \"types\" : [\n" +
            "            \"night_club\",\n" +
            "            \"bar\",\n" +
            "            \"restaurant\",\n" +
            "            \"food\",\n" +
            "            \"point_of_interest\",\n" +
            "            \"establishment\"\n" +
            "         ],\n" +
            "         \"user_ratings_total\" : 734,\n" +
            "         \"vicinity\" : \"198 Darling Drive, Sydney\"\n" +
            "      }\n" +
            "   ],\n" +
            "   \"status\" : \"OK\"\n" +
            "}";

    public static final String placeDetailResponse = "{\n" +
            "   \"html_attributions\" : [],\n" +
            "   \"result\" : {\n" +
            "      \"address_components\" : [\n" +
            "         {\n" +
            "            \"long_name\" : \"3\",\n" +
            "            \"short_name\" : \"3\",\n" +
            "            \"types\" : [ \"subpremise\" ]\n" +
            "         },\n" +
            "         {\n" +
            "            \"long_name\" : \"50\",\n" +
            "            \"short_name\" : \"50\",\n" +
            "            \"types\" : [ \"street_number\" ]\n" +
            "         },\n" +
            "         {\n" +
            "            \"long_name\" : \"Murray Street\",\n" +
            "            \"short_name\" : \"Murray St\",\n" +
            "            \"types\" : [ \"route\" ]\n" +
            "         },\n" +
            "         {\n" +
            "            \"long_name\" : \"Pyrmont\",\n" +
            "            \"short_name\" : \"Pyrmont\",\n" +
            "            \"types\" : [ \"locality\", \"political\" ]\n" +
            "         },\n" +
            "         {\n" +
            "            \"long_name\" : \"City of Sydney\",\n" +
            "            \"short_name\" : \"City of Sydney\",\n" +
            "            \"types\" : [ \"administrative_area_level_2\", \"political\" ]\n" +
            "         },\n" +
            "         {\n" +
            "            \"long_name\" : \"New South Wales\",\n" +
            "            \"short_name\" : \"NSW\",\n" +
            "            \"types\" : [ \"administrative_area_level_1\", \"political\" ]\n" +
            "         },\n" +
            "         {\n" +
            "            \"long_name\" : \"Australie\",\n" +
            "            \"short_name\" : \"AU\",\n" +
            "            \"types\" : [ \"country\", \"political\" ]\n" +
            "         },\n" +
            "         {\n" +
            "            \"long_name\" : \"2009\",\n" +
            "            \"short_name\" : \"2009\",\n" +
            "            \"types\" : [ \"postal_code\" ]\n" +
            "         }\n" +
            "      ],\n" +
            "      \"adr_address\" : \"3/\\u003cspan class=\\\"street-address\\\"\\u003e50 Murray St\\u003c/span\\u003e, \\u003cspan class=\\\"locality\\\"\\u003ePyrmont\\u003c/span\\u003e \\u003cspan class=\\\"region\\\"\\u003eNSW\\u003c/span\\u003e \\u003cspan class=\\\"postal-code\\\"\\u003e2009\\u003c/span\\u003e, \\u003cspan class=\\\"country-name\\\"\\u003eAustralie\\u003c/span\\u003e\",\n" +
            "      \"business_status\" : \"OPERATIONAL\",\n" +
            "      \"formatted_address\" : \"3/50 Murray St, Pyrmont NSW 2009, Australie\",\n" +
            "      \"formatted_phone_number\" : \"(02) 9212 7512\",\n" +
            "      \"geometry\" : {\n" +
            "         \"location\" : {\n" +
            "            \"lat\" : -33.8703417,\n" +
            "            \"lng\" : 151.1979222\n" +
            "         },\n" +
            "         \"viewport\" : {\n" +
            "            \"northeast\" : {\n" +
            "               \"lat\" : -33.86900816970849,\n" +
            "               \"lng\" : 151.1991662802915\n" +
            "            },\n" +
            "            \"southwest\" : {\n" +
            "               \"lat\" : -33.87170613029149,\n" +
            "               \"lng\" : 151.1964683197085\n" +
            "            }\n" +
            "         }\n" +
            "      },\n" +
            "      \"icon\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/restaurant-71.png\",\n" +
            "      \"icon_background_color\" : \"#FF9E67\",\n" +
            "      \"icon_mask_base_uri\" : \"https://maps.gstatic.com/mapfiles/place_api/icons/v2/restaurant_pinlet\",\n" +
            "      \"international_phone_number\" : \"+61 2 9212 7512\",\n" +
            "      \"name\" : \"The Little Snail Restaurant\",\n" +
            "      \"opening_hours\" : {\n" +
            "         \"open_now\" : false,\n" +
            "         \"periods\" : [\n" +
            "            {\n" +
            "               \"close\" : {\n" +
            "                  \"day\" : 0,\n" +
            "                  \"time\" : \"1500\"\n" +
            "               },\n" +
            "               \"open\" : {\n" +
            "                  \"day\" : 0,\n" +
            "                  \"time\" : \"1100\"\n" +
            "               }\n" +
            "            },\n" +
            "            {\n" +
            "               \"close\" : {\n" +
            "                  \"day\" : 0,\n" +
            "                  \"time\" : \"2100\"\n" +
            "               },\n" +
            "               \"open\" : {\n" +
            "                  \"day\" : 0,\n" +
            "                  \"time\" : \"1700\"\n" +
            "               }\n" +
            "            },\n" +
            "            {\n" +
            "               \"close\" : {\n" +
            "                  \"day\" : 1,\n" +
            "                  \"time\" : \"1500\"\n" +
            "               },\n" +
            "               \"open\" : {\n" +
            "                  \"day\" : 1,\n" +
            "                  \"time\" : \"1100\"\n" +
            "               }\n" +
            "            },\n" +
            "            {\n" +
            "               \"close\" : {\n" +
            "                  \"day\" : 1,\n" +
            "                  \"time\" : \"2100\"\n" +
            "               },\n" +
            "               \"open\" : {\n" +
            "                  \"day\" : 1,\n" +
            "                  \"time\" : \"1700\"\n" +
            "               }\n" +
            "            },\n" +
            "            {\n" +
            "               \"close\" : {\n" +
            "                  \"day\" : 2,\n" +
            "                  \"time\" : \"1500\"\n" +
            "               },\n" +
            "               \"open\" : {\n" +
            "                  \"day\" : 2,\n" +
            "                  \"time\" : \"1100\"\n" +
            "               }\n" +
            "            },\n" +
            "            {\n" +
            "               \"close\" : {\n" +
            "                  \"day\" : 2,\n" +
            "                  \"time\" : \"2100\"\n" +
            "               },\n" +
            "               \"open\" : {\n" +
            "                  \"day\" : 2,\n" +
            "                  \"time\" : \"1700\"\n" +
            "               }\n" +
            "            },\n" +
            "            {\n" +
            "               \"close\" : {\n" +
            "                  \"day\" : 3,\n" +
            "                  \"time\" : \"1500\"\n" +
            "               },\n" +
            "               \"open\" : {\n" +
            "                  \"day\" : 3,\n" +
            "                  \"time\" : \"1100\"\n" +
            "               }\n" +
            "            },\n" +
            "            {\n" +
            "               \"close\" : {\n" +
            "                  \"day\" : 3,\n" +
            "                  \"time\" : \"2100\"\n" +
            "               },\n" +
            "               \"open\" : {\n" +
            "                  \"day\" : 3,\n" +
            "                  \"time\" : \"1700\"\n" +
            "               }\n" +
            "            },\n" +
            "            {\n" +
            "               \"close\" : {\n" +
            "                  \"day\" : 4,\n" +
            "                  \"time\" : \"1500\"\n" +
            "               },\n" +
            "               \"open\" : {\n" +
            "                  \"day\" : 4,\n" +
            "                  \"time\" : \"1100\"\n" +
            "               }\n" +
            "            },\n" +
            "            {\n" +
            "               \"close\" : {\n" +
            "                  \"day\" : 4,\n" +
            "                  \"time\" : \"2100\"\n" +
            "               },\n" +
            "               \"open\" : {\n" +
            "                  \"day\" : 4,\n" +
            "                  \"time\" : \"1700\"\n" +
            "               }\n" +
            "            },\n" +
            "            {\n" +
            "               \"close\" : {\n" +
            "                  \"day\" : 5,\n" +
            "                  \"time\" : \"1500\"\n" +
            "               },\n" +
            "               \"open\" : {\n" +
            "                  \"day\" : 5,\n" +
            "                  \"time\" : \"1100\"\n" +
            "               }\n" +
            "            },\n" +
            "            {\n" +
            "               \"close\" : {\n" +
            "                  \"day\" : 5,\n" +
            "                  \"time\" : \"2200\"\n" +
            "               },\n" +
            "               \"open\" : {\n" +
            "                  \"day\" : 5,\n" +
            "                  \"time\" : \"1700\"\n" +
            "               }\n" +
            "            },\n" +
            "            {\n" +
            "               \"close\" : {\n" +
            "                  \"day\" : 6,\n" +
            "                  \"time\" : \"1500\"\n" +
            "               },\n" +
            "               \"open\" : {\n" +
            "                  \"day\" : 6,\n" +
            "                  \"time\" : \"1100\"\n" +
            "               }\n" +
            "            },\n" +
            "            {\n" +
            "               \"close\" : {\n" +
            "                  \"day\" : 6,\n" +
            "                  \"time\" : \"2200\"\n" +
            "               },\n" +
            "               \"open\" : {\n" +
            "                  \"day\" : 6,\n" +
            "                  \"time\" : \"1700\"\n" +
            "               }\n" +
            "            }\n" +
            "         ],\n" +
            "         \"weekday_text\" : [\n" +
            "            \"lundi: 11:00 – 15:00, 17:00 – 21:00\",\n" +
            "            \"mardi: 11:00 – 15:00, 17:00 – 21:00\",\n" +
            "            \"mercredi: 11:00 – 15:00, 17:00 – 21:00\",\n" +
            "            \"jeudi: 11:00 – 15:00, 17:00 – 21:00\",\n" +
            "            \"vendredi: 11:00 – 15:00, 17:00 – 22:00\",\n" +
            "            \"samedi: 11:00 – 15:00, 17:00 – 22:00\",\n" +
            "            \"dimanche: 11:00 – 15:00, 17:00 – 21:00\"\n" +
            "         ]\n" +
            "      },\n" +
            "      \"photos\" : [\n" +
            "         {\n" +
            "            \"height\" : 900,\n" +
            "            \"html_attributions\" : [\n" +
            "               \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/114727320476039103791\\\"\\u003eThe Little Snail Restaurant\\u003c/a\\u003e\"\n" +
            "            ],\n" +
            "            \"photo_reference\" : \"Aap_uECHOY9tHwTVHBTtSGu47nYknO1MQlLm7RsWIvB4twBy8nW0zE2-8IanRy9zZ0GVvo5hAudZHtK7KE0i5Zc_nPThnDaCT9bz-rvVcCoPb-4PxSt6ojI7jEEUE66n6UUF5PnKea7caMT0BgU5JMS9B752Un4UY1f6jp_sZv9XIXuoi8rx\",\n" +
            "            \"width\" : 1350\n" +
            "         },\n" +
            "         {\n" +
            "            \"height\" : 900,\n" +
            "            \"html_attributions\" : [\n" +
            "               \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/114727320476039103791\\\"\\u003eThe Little Snail Restaurant\\u003c/a\\u003e\"\n" +
            "            ],\n" +
            "            \"photo_reference\" : \"Aap_uEAoMbAKgGMH2crm8z_dJnHoCed0BVqe3MEm08xqISozp7p-1VPgejiP3w5GTjfMxGl8EqUKPTLH8teQVFwTBMm6cTxJNL5J_qWcHGZDY4rWPZ_-7WHspl9ZHHyDRQC_82ElhgX1toszGy_OP4FfrTC2jqDbe9hO3iLb8OU8c_pxRFSG\",\n" +
            "            \"width\" : 1350\n" +
            "         },\n" +
            "         {\n" +
            "            \"height\" : 2328,\n" +
            "            \"html_attributions\" : [\n" +
            "               \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/118215596461806548270\\\"\\u003eNazer Sangalang III\\u003c/a\\u003e\"\n" +
            "            ],\n" +
            "            \"photo_reference\" : \"Aap_uEDB3rF1_Xe8Reg-u22h9qynV_0VrvqVgbYzC1SivPPlnoZqE8CkMPdgarcpw9BuVZ9YMt3f2O1_m05Z7JKYNrunNTEyo4BeslO8cgFQZoDoAYVXxYR7PNvyDsto0Rndq0BLkHqTCOHGyNfmmjkvbwiX3nQJbjrnSBzuMzGYbWBZR3fO\",\n" +
            "            \"width\" : 4656\n" +
            "         },\n" +
            "         {\n" +
            "            \"height\" : 900,\n" +
            "            \"html_attributions\" : [\n" +
            "               \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/114727320476039103791\\\"\\u003eThe Little Snail Restaurant\\u003c/a\\u003e\"\n" +
            "            ],\n" +
            "            \"photo_reference\" : \"Aap_uEA6iBpjiA4UIMUVlaAX1J1xSX1UTrqiAubB0qFpm6LiaM_TswOfci_ItOUQw2Z_KnYwc6vZQNa3Tq4LNoncjr1OawvrMHKYN-FqSYdDcv78zZXASrNqqQLYMBTRAyvRLnWYe9aT2XYvr3Fxsan3oPGL-PS-ea7QYntI7Qgl9Y17_-DV\",\n" +
            "            \"width\" : 1350\n" +
            "         },\n" +
            "         {\n" +
            "            \"height\" : 3024,\n" +
            "            \"html_attributions\" : [\n" +
            "               \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/110564135651640413434\\\"\\u003eKevin H\\u003c/a\\u003e\"\n" +
            "            ],\n" +
            "            \"photo_reference\" : \"Aap_uEDteo8IdtB6IH4QJYiGYMZPlxKpcuqhVQaJLFwIpIIufK9YBc5Ntc-DfJMYME115yWZlgnsxLy0_P2z-LlnyhCXqv9lvncYdvWrK4Abl3X8jUrKaJycUaGc6y6Z2lmnOJ6XMJlWvu4hl7-DPJNBYrD668rQlu6ioDaYSN8C7uWN5aU\",\n" +
            "            \"width\" : 4032\n" +
            "         },\n" +
            "         {\n" +
            "            \"height\" : 3024,\n" +
            "            \"html_attributions\" : [\n" +
            "               \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/102873861577055782875\\\"\\u003eSir Mlady\\u003c/a\\u003e\"\n" +
            "            ],\n" +
            "            \"photo_reference\" : \"Aap_uEC83mcq-bgQVWHDCX8nBdsbhIR7v1ZaM4PgCKcO4wqedBMMLJOP4RonLcgoJ4tgg1tTveMb1HD4WpugkHxq4z1WLLD-3BB7EiGPaRdacwGs1OscOsEdGrDSI6Luv27b07bTz35l7Wgw8kAvUwLQ_wAk1PEByL1PYZt9WsjrdeXTG-MQ\",\n" +
            "            \"width\" : 4032\n" +
            "         },\n" +
            "         {\n" +
            "            \"height\" : 3024,\n" +
            "            \"html_attributions\" : [\n" +
            "               \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/110564135651640413434\\\"\\u003eKevin H\\u003c/a\\u003e\"\n" +
            "            ],\n" +
            "            \"photo_reference\" : \"Aap_uEBVTVO_74TmNilok6CR4IQvtaT1sf-ExYj2whQ7rIOqUUdgJfcFYQa_ebV0PUY4uDycqWAMZgEmIhlQuxTtmUCO31VrOyrBfmCH7gFPiVgM_Knla6OpsHDmCOsxZl-CtT3jQVzWl_UbrEsrsLbVnqaFgoV9aun0hlWzSQl1L4qpz84\",\n" +
            "            \"width\" : 4032\n" +
            "         },\n" +
            "         {\n" +
            "            \"height\" : 3024,\n" +
            "            \"html_attributions\" : [\n" +
            "               \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/112924876191440381752\\\"\\u003eBeeYan &amp; King\\u003c/a\\u003e\"\n" +
            "            ],\n" +
            "            \"photo_reference\" : \"Aap_uEC5kXO1zXlkCz5_KmQb64L901CkgtIrhEQJ5TUlle2NfYdLeu_CIosHdpcDaKEN5ULigfJ7QTpdZeHcSQcKJP1mhQp4vxzJP9ztzDst9LRgl9JHgA_Jzz8ce7rJhTfc_1cFkpnM3qiuWsucowIIAyLLyXcNJpeI6TLMRCw6MGl2Y0gJ\",\n" +
            "            \"width\" : 4032\n" +
            "         },\n" +
            "         {\n" +
            "            \"height\" : 3024,\n" +
            "            \"html_attributions\" : [\n" +
            "               \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/113731454023377758804\\\"\\u003eDushmantha Walakulpola\\u003c/a\\u003e\"\n" +
            "            ],\n" +
            "            \"photo_reference\" : \"Aap_uEBeUzt_885Lm208tF5XWGJvFcopSC_9PE-xQFSAczUpJXP2sWhcVB_oMVbFCdBUR1zwfCQhreAgQ8m87uEkc9WEZjo9nhVuhnBEqduuHKR9elPHHH3MCvePMuqsk4CWtLDe9iMNqwfoBkhOqNdusj3GHsEKF3Y4T4jFw49cer3KaORG\",\n" +
            "            \"width\" : 4032\n" +
            "         },\n" +
            "         {\n" +
            "            \"height\" : 3024,\n" +
            "            \"html_attributions\" : [\n" +
            "               \"\\u003ca href=\\\"https://maps.google.com/maps/contrib/118400063929421044479\\\"\\u003eJanson Cheng\\u003c/a\\u003e\"\n" +
            "            ],\n" +
            "            \"photo_reference\" : \"Aap_uECGmPtDbJijlHCAIqnkl4FLNfUt4mycU0xPfti9CAjpsz8lmrw66HfSUSHNe4LJYhybWXWzBiF6xQ_i8Av0OHTMRCEJ0K9lRiXEk4r78Oz7g0Z6Y13_VxR6aj6qPNxkgZeduXCX_CboH0IsodCUP9yl4NZtseu1VezrGm8OyUjNcV3S\",\n" +
            "            \"width\" : 4032\n" +
            "         }\n" +
            "      ],\n" +
            "      \"place_id\" : \"ChIJtwapWjeuEmsRcxV5JARHpSk\",\n" +
            "      \"plus_code\" : {\n" +
            "         \"compound_code\" : \"45HX+V5 Pyrmont Nouvelle-Galles du Sud, Australie\",\n" +
            "         \"global_code\" : \"4RRH45HX+V5\"\n" +
            "      },\n" +
            "      \"price_level\" : 2,\n" +
            "      \"rating\" : 4.5,\n" +
            "      \"reference\" : \"ChIJtwapWjeuEmsRcxV5JARHpSk\",\n" +
            "      \"reviews\" : [\n" +
            "         {\n" +
            "            \"author_name\" : \"Rom Bobo\",\n" +
            "            \"author_url\" : \"https://www.google.com/maps/contrib/108525676249535919317/reviews\",\n" +
            "            \"language\" : \"fr\",\n" +
            "            \"profile_photo_url\" : \"https://lh3.googleusercontent.com/a/AATXAJz2fGFGw3i3YPeUU7wSEY8z7g5cR4qI8nX1DHZM=s128-c0x00000000-cc-rp-mo\",\n" +
            "            \"rating\" : 1,\n" +
            "            \"relative_time_description\" : \"il y a un an\",\n" +
            "            \"text\" : \"1 étoile pour le service sympathique.\\nAttrape touristes total. Paté sans goût, profiteroles made in Coles probablement, Magret de canard et épinard ? (Ou sont les pommes de terre)\\nFuyez\",\n" +
            "            \"time\" : 1603428962\n" +
            "         },\n" +
            "         {\n" +
            "            \"author_name\" : \"Sabrina Bourouis\",\n" +
            "            \"author_url\" : \"https://www.google.com/maps/contrib/107594221777080812888/reviews\",\n" +
            "            \"language\" : \"fr\",\n" +
            "            \"profile_photo_url\" : \"https://lh3.googleusercontent.com/a-/AOh14Gg0KOOAI4tQKhw4aaO0xSrUnWlufMw9VVnH9ne4dQ=s128-c0x00000000-cc-rp-mo-ba4\",\n" +
            "            \"rating\" : 1,\n" +
            "            \"relative_time_description\" : \"il y a 9 mois\",\n" +
            "            \"text\" : \"Je suis venue avec une offre Groupon. Tous les plats que j'ai commandés manquaient cruellement d'assaisonnement. La bouillabaisse était fade, les fruits de mers trop cuits. Le veau avec une sauce aux champignons était accompagné d'une seule croquette de pomme de terre et il n'y avait pas de pain pour saucer. La mousse au chocolat était hyper compacte, pas aérienne. Les profiteroles étaient sèches et la crème pâtissière n'avait pas le goût de vanille. Le service était néanmoins rapide et le vin était bon. J'étais malade en rentrant. Je ne reviendrai pas.\",\n" +
            "            \"time\" : 1616710851\n" +
            "         },\n" +
            "         {\n" +
            "            \"author_name\" : \"Foued Maazaoui\",\n" +
            "            \"author_url\" : \"https://www.google.com/maps/contrib/101530112732605221037/reviews\",\n" +
            "            \"language\" : \"fr\",\n" +
            "            \"profile_photo_url\" : \"https://lh3.googleusercontent.com/a/AATXAJyyIq9q2RCzJeMkVl5Aia7HEY-bmAiHPH9BJz58=s128-c0x00000000-cc-rp-mo\",\n" +
            "            \"rating\" : 1,\n" +
            "            \"relative_time_description\" : \"il y a un an\",\n" +
            "            \"text\" : \"Attrape touriste chinois, une honte pour le prix demandé.\",\n" +
            "            \"time\" : 1605996581\n" +
            "         },\n" +
            "         {\n" +
            "            \"author_name\" : \"Olfa Howell\",\n" +
            "            \"author_url\" : \"https://www.google.com/maps/contrib/106916091619982895584/reviews\",\n" +
            "            \"language\" : \"fr\",\n" +
            "            \"profile_photo_url\" : \"https://lh3.googleusercontent.com/a/AATXAJwh5tLPaPQtGrTzPWOwt3W-MVAaCEV4dgtfxdxt=s128-c0x00000000-cc-rp-mo-ba4\",\n" +
            "            \"rating\" : 5,\n" +
            "            \"relative_time_description\" : \"il y a 3 ans\",\n" +
            "            \"text\" : \"Très bon, service très agréable et lieu chaleureux\",\n" +
            "            \"time\" : 1544092576\n" +
            "         },\n" +
            "         {\n" +
            "            \"author_name\" : \"Raymund Wong\",\n" +
            "            \"author_url\" : \"https://www.google.com/maps/contrib/103845429580210592744/reviews\",\n" +
            "            \"language\" : \"fr-FR\",\n" +
            "            \"profile_photo_url\" : \"https://lh3.googleusercontent.com/a-/AOh14Gi_iWsDcQ01SIgS-R8_A_EpKQLY8OAgqn_IoRfWFg=s128-c0x00000000-cc-rp-mo-ba5\",\n" +
            "            \"rating\" : 4,\n" +
            "            \"relative_time_description\" : \"il y a un mois\",\n" +
            "            \"text\" : \"La nourriture est bonne ici et le dessert est bon aussi. Nous aimons l'escargot français ici.\\n\\nLe café n'est pas très agréable ici et a besoin d'être amélioré.\\n\\nLe service est très bon. Les dames qui nous ont servi sont très attentionnées et aux petits soins.\\n\\nDans l'ensemble, pour le prix que vous avez payé, c'est bon !\\n\\nJuste une note, cet endroit n'est pas adapté aux personnes handicapées, il n'y a pas de toilettes pour personnes handicapées et l'accès au restaurant est un problème.\\n\\nConseillé.\",\n" +
            "            \"time\" : 1637045118\n" +
            "         }\n" +
            "      ],\n" +
            "      \"types\" : [ \"restaurant\", \"food\", \"point_of_interest\", \"establishment\" ],\n" +
            "      \"url\" : \"https://maps.google.com/?cid=3000882809829660019\",\n" +
            "      \"user_ratings_total\" : 1764,\n" +
            "      \"utc_offset\" : 660,\n" +
            "      \"vicinity\" : \"3/50 Murray Street, Pyrmont\",\n" +
            "      \"website\" : \"http://www.thelittlesnail.com.au/\"\n" +
            "   },\n" +
            "   \"status\" : \"OK\"\n" +
            "}";
}
