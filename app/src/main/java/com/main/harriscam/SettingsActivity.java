package com.main.harriscam;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.main.harriscam.util.HarrisConfig;

public class SettingsActivity extends PreferenceActivity {

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener
            = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange( Preference preference, Object value ) {

            if ( preference instanceof ListPreference ) {
                String stringValue = value.toString();
                ListPreference listPreference = ( ListPreference ) preference;
                int index = listPreference.findIndexOfValue( stringValue );

                preference.setSummary( index >= 0 ? listPreference.getEntries()[ index ] : null );
            }

            return true;
        }
    };

    private static void bindPreferenceSummaryToValue( Preference preference ) {
        preference.setOnPreferenceChangeListener( sBindPreferenceSummaryToValueListener );

        sBindPreferenceSummaryToValueListener.onPreferenceChange( preference,
                PreferenceManager.getDefaultSharedPreferences( preference.getContext() ).getString( preference.getKey(), "" ) );
    }

    @Override
    protected void onPostCreate( Bundle savedInstanceState ) {
        super.onPostCreate( savedInstanceState );

        getActionBar().setHomeButtonEnabled( true );

        setupSimplePreferencesScreen();
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch ( item.getItemId() ) {
            case android.R.id.home:
                finish();

                return true;
        }
        return super.onOptionsItemSelected( item );
    }

    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource( R.xml.pref_general );
        addPreferencesFromResource( R.xml.pref_about );

        CharSequence[] quality_list = new CharSequence[ HarrisConfig.PHOTO_QUALITY.size() ];
        CharSequence[] quality_list_values = new CharSequence[ HarrisConfig.PHOTO_QUALITY.size() ];
        for ( int i = 0; i < quality_list.length; i++ ) {
            quality_list[ i ] = HarrisConfig.PHOTO_QUALITY.get( i ).width + " x " + HarrisConfig.PHOTO_QUALITY.get( i ).height;
            quality_list_values[ i ] = String.valueOf( i );
        }

        ( ( ListPreference ) findPreference( getString( R.string.pref_id_quality ) ) ).setEntries( quality_list );
        ( ( ListPreference ) findPreference( getString( R.string.pref_id_quality ) ) ).setEntryValues( quality_list_values );

        bindPreferenceSummaryToValue( findPreference( getString( R.string.pref_id_quality ) ) );
        bindPreferenceSummaryToValue( findPreference( getString( R.string.pref_id_storage ) ) );
    }
}
