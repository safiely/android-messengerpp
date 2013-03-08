package org.solovyev.android.messenger.realms;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.fragments.AbstractFragmentReuseCondition;
import org.solovyev.common.JPredicate;
import roboguice.event.EventListener;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 1:50 PM
 */
public class RealmGuiEventListener implements EventListener<RealmGuiEvent> {

    @Nonnull
    private static final String TAG = RealmGuiEventListener.class.getSimpleName();

    @Nonnull
    private final MessengerFragmentActivity activity;

    public RealmGuiEventListener(@Nonnull MessengerFragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onEvent(@Nonnull RealmGuiEvent event) {
        final Realm realm = event.getRealm();

        switch (event.getType()) {
            case realm_view_requested:
                handleRealmViewRequestedEvent(realm);
                break;
            case realm_view_cancelled:
                handleRealmViewCancelledEvent(realm);
                break;
            case realm_edit_requested:
                handleRealmEditRequestedEvent(realm);
                break;
            case realm_edit_finished:
                handleRealmEditFinishedEvent(event);
                break;
        }
    }

    private void handleRealmViewCancelledEvent(@Nonnull Realm realm) {
        activity.getSupportFragmentManager().popBackStack();
    }

    private void handleRealmEditRequestedEvent(@Nonnull Realm realm) {
        final Bundle fragmentArgs = new Bundle();
        fragmentArgs.putString(BaseRealmConfigurationFragment.EXTRA_REALM_ID, realm.getId());
        if (activity.isDualPane()) {
            activity.getFragmentService().setSecondFragment(realm.getRealmDef().getConfigurationFragmentClass(), fragmentArgs, null, BaseRealmConfigurationFragment.FRAGMENT_TAG, true);
        } else {
            activity.getFragmentService().setFirstFragment(realm.getRealmDef().getConfigurationFragmentClass(), fragmentArgs, null, BaseRealmConfigurationFragment.FRAGMENT_TAG, true);
        }
    }

    private void handleRealmViewRequestedEvent(@Nonnull Realm realm) {
        if (activity.isDualPane()) {
            showRealmFragment(realm, false);
            if ( activity.isTriplePane() ) {
                activity.getFragmentService().emptifyThirdFragment();
            }
        } else {
            showRealmFragment(realm, true);
        }
    }

    private void showRealmFragment(@Nonnull Realm realm, boolean firstPane) {
        final Bundle fragmentArgs = new Bundle();
        fragmentArgs.putString(MessengerRealmFragment.EXTRA_REALM_ID, realm.getId());
        if (firstPane) {
            activity.getFragmentService().setFirstFragment(MessengerRealmFragment.class, fragmentArgs, RealmFragmentReuseCondition.forRealm(realm), MessengerRealmFragment.FRAGMENT_TAG, true);
        } else {
            activity.getFragmentService().setSecondFragment(MessengerRealmFragment.class, fragmentArgs, RealmFragmentReuseCondition.forRealm(realm), MessengerRealmFragment.FRAGMENT_TAG, false);
        }
    }

    private void handleRealmEditFinishedEvent(@Nonnull RealmGuiEvent event) {
        final RealmGuiEventType.FinishedState state = (RealmGuiEventType.FinishedState) event.getData();
        assert state != null;
        switch (state) {
            case back:
                activity.getFragmentService().goBack();
                break;
            case removed:
                activity.getFragmentService().goBackTillStart();
                if ( activity.isDualPane() ) {
                    activity.getFragmentService().emptifySecondFragment();
                }
                break;
            case saved:
                activity.getFragmentService().goBackTillStart();
                break;
        }
    }

    /*
    **********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

    /**
     * Fragment will be reused if it's instance of {@link MessengerRealmFragment} and
     * contains same realm as one passed in constructor
     */
    private static class RealmFragmentReuseCondition extends AbstractFragmentReuseCondition<MessengerRealmFragment> {

        @Nonnull
        private final Realm realm;

        private RealmFragmentReuseCondition(@Nonnull Realm realm) {
            super(MessengerRealmFragment.class);
            this.realm = realm;
        }

        @Nonnull
        public static JPredicate<Fragment> forRealm(@Nonnull Realm realm) {
            return new RealmFragmentReuseCondition(realm);
        }

        @Override
        protected boolean canReuseFragment(@Nonnull MessengerRealmFragment fragment) {
            return realm.equals(fragment.getRealm());
        }
    }
}
