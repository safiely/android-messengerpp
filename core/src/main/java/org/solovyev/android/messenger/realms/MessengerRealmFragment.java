package org.solovyev.android.messenger.realms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import org.solovyev.android.messenger.MessengerMultiPaneManager;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.sync.MessengerSyncAllAsyncTask;
import org.solovyev.android.messenger.sync.SyncService;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/1/13
 * Time: 8:57 PM
 */
public class MessengerRealmFragment extends RoboSherlockFragment {

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

    @Nonnull
    public static final String EXTRA_REALM_ID = "realm_id";

    @Nonnull
    public static final String FRAGMENT_TAG = "realm";

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @Nonnull
    private RealmService realmService;

    @Inject
    @Nonnull
    private SyncService syncService;


    @Inject
    @Nonnull
    private MessengerMultiPaneManager multiPaneManager;

    @Inject
    @Nonnull
    private EventManager eventManager;

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

    private Realm realm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            final String realmId = arguments.getString(EXTRA_REALM_ID);
            if (realmId != null) {
                realm = realmService.getRealmById(realmId);
            }
        }

        if (realm == null) {
            // remove fragment
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View result = ViewFromLayoutBuilder.newInstance(R.layout.mpp_fragment_realm).build(this.getActivity());

        multiPaneManager.onCreatePane(this.getActivity(), container, result);

        result.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return result;
    }

    @Override
    public void onViewCreated(@Nonnull View root, Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        final ImageView realmIconImageView = (ImageView) root.findViewById(R.id.mpp_realm_def_icon_imageview);
        realmIconImageView.setImageDrawable(getResources().getDrawable(realm.getRealmDef().getIconResId()));

        final TextView realmNameTextView = (TextView) root.findViewById(R.id.mpp_fragment_title);
        realmNameTextView.setText(realm.getDisplayName(getActivity()));

        final Button realmBackButton = (Button) root.findViewById(R.id.mpp_realm_back_button);
        realmBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventManager.fire(RealmGuiEventType.newRealmViewCancelledEvent(realm));
            }
        });
        if (multiPaneManager.isDualPane(getActivity())) {
            // in multi pane layout we don't want to show 'Back' button as there is no 'Back' (in one pane we reuse pane for showing more than one fragment and back means to return to the previous fragment)
            realmBackButton.setVisibility(View.GONE);
        } else {
            realmBackButton.setVisibility(View.VISIBLE);
        }

        final Button realmRemoveButton = (Button) root.findViewById(R.id.mpp_realm_remove_button);
         realmRemoveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeRealm();
                }
            });

        final Button realmEditButton = (Button) root.findViewById(R.id.mpp_realm_edit_button);
        realmEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editRealm();
            }
        });

        final Button realmSyncButton = (Button) root.findViewById(R.id.mpp_realm_sync_button);
        realmSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessengerSyncAllAsyncTask.newForRealm(getActivity(), syncService, realm).execute((Void)null);
            }
        });

        multiPaneManager.onPaneCreated(getActivity(), root);
    }

    private void editRealm() {
        eventManager.fire(RealmGuiEventType.newRealmEditRequestedEvent(realm));
    }

    @Nonnull
    public Realm getRealm() {
        return realm;
    }


    private void removeRealm() {
        new AsynRealmRemover(this.getActivity(), realmService).execute(realm);
    }
}
