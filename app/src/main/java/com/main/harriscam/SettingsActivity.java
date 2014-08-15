package com.main.harriscam;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.main.harriscam.util.HarrisConfig;
import com.main.harriscam.util.HarrisUtil;

public class SettingsActivity extends PreferenceActivity {

    private static Preference.OnPreferenceChangeListener listenerPreferenceChange
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

    private static Preference.OnPreferenceClickListener listenerPreferenceClick = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick( Preference preference ) {
            if ( preference.getKey().equals( preference.getContext().getString( R.string.pref_id_feedback ) ) )
                preference.getContext().startActivity( new Intent( preference.getContext(), FeedbackActivity.class ) );

            return false;
        }
    };

    private Preference.OnPreferenceChangeListener listenerPreferenceCheckChange =
            new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange( Preference preference, Object checked ) {
                    if ( Boolean.parseBoolean( checked.toString() ) ) {
                        HarrisUtil.jlog( Boolean.parseBoolean( checked.toString() ) );
                        if ( preference.getKey().equals( preference.getContext().getString( R.string.pref_id_save_original ) ) )
                            ( ( CheckBoxPreference ) findPreference( getString( R.string.pref_id_save_original_of_all ) ) ).setChecked( false );
                        else
                            ( ( CheckBoxPreference ) findPreference( getString( R.string.pref_id_save_original ) ) ).setChecked( false );
                    }

                    return true;
                }
            };

    private static void bindPreferenceSummaryToValue( Preference preference ) {
        preference.setOnPreferenceChangeListener( listenerPreferenceChange );

        listenerPreferenceChange.onPreferenceChange( preference,
                PreferenceManager.getDefaultSharedPreferences( preference.getContext() ).getString( preference.getKey(), "" ) );
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        try {
            getActionBar().setHomeButtonEnabled( true );
        } catch ( Exception e ) {
            HarrisUtil.jlog( "On Preference : " + e.toString() );
        }

        setupPreferencesScreen();
    }

    @Override
    public void onWindowFocusChanged( boolean hasFocus ) {
        ViewGroup parent = ( ViewGroup ) getListView().getParent();
        parent.setPadding( 0, 0, 0, 0 );

        super.onWindowFocusChanged( hasFocus );
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

    private void setupPreferencesScreen() {
        addPreferencesFromResource( R.xml.pref_general );
        addPreferencesFromResource( R.xml.pref_about );

        CharSequence[] quality_list = new CharSequence[ HarrisConfig.PHOTO_QUALITY_LIST.size() ];
        CharSequence[] quality_list_values = new CharSequence[ HarrisConfig.PHOTO_QUALITY_LIST.size() ];
        for ( int i = 0; i < quality_list.length; i++ ) {
            quality_list[ i ] = HarrisConfig.PHOTO_QUALITY_LIST.get( i ).width + " x " + HarrisConfig.PHOTO_QUALITY_LIST.get( i ).height;
            quality_list_values[ i ] = String.valueOf( i );
        }

        ( ( ListPreference ) findPreference( getString( R.string.pref_id_quality ) ) ).setEntries( quality_list );
        ( ( ListPreference ) findPreference( getString( R.string.pref_id_quality ) ) ).setEntryValues( quality_list_values );

        CharSequence[] storage_list = new CharSequence[ HarrisConfig.STORAGE_PATH_LIST.size() ];
        CharSequence[] storage_list_values = new CharSequence[ HarrisConfig.STORAGE_PATH_LIST.size() ];
        for ( int i = 0; i < storage_list.length; i++ ) {
            storage_list[ i ] = getResources().getStringArray( R.array.pref_list_storage )[ i ];
            storage_list_values[ i ] = String.valueOf( i );
        }
        ( ( ListPreference ) findPreference( getString( R.string.pref_id_storage ) ) ).setEntries( storage_list );
        ( ( ListPreference ) findPreference( getString( R.string.pref_id_storage ) ) ).setEntryValues( storage_list_values );


        bindPreferenceSummaryToValue( findPreference( getString( R.string.pref_id_quality ) ) );
        bindPreferenceSummaryToValue( findPreference( getString( R.string.pref_id_storage ) ) );

        findPreference( getString( R.string.pref_id_feedback ) ).setOnPreferenceClickListener( listenerPreferenceClick );

        findPreference( getString( R.string.pref_id_save_original ) ).setOnPreferenceChangeListener( listenerPreferenceCheckChange );
        findPreference( getString( R.string.pref_id_save_original_of_all ) ).setOnPreferenceChangeListener( listenerPreferenceCheckChange );
    }
}
