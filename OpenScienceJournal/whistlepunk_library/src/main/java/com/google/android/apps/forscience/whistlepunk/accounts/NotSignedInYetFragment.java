/*
 *  Copyright 2018 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.android.apps.forscience.whistlepunk.accounts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.android.apps.forscience.whistlepunk.MainActivity;
import com.google.android.apps.forscience.whistlepunk.R;
import com.google.android.apps.forscience.whistlepunk.RxEvent;
import com.google.android.apps.forscience.whistlepunk.WhistlePunkApplication;

/** Fragment that tells the user to explore their world. */
public class NotSignedInYetFragment extends Fragment {
  private static final String KEY_OLD_USER_OPTION_PROMPT_SHOWN = "key_old_user_option_prompt_shown";

  private static final int REQUEST_CODE_ACCOUNT_SWITCHER = 217;

  private final RxEvent pause = new RxEvent();
  private AccountsProvider accountsProvider;
  private AppAccount currentAccount;
  private boolean showingAccountSwitcherDialog;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    accountsProvider = WhistlePunkApplication.getAppServices(getActivity()).getAccountsProvider();

    View view = inflater.inflate(R.layout.fragment_not_signed_in_yet, container, false);

    Button getStartedButton = view.findViewById(R.id.btn_get_started);
    getStartedButton.setOnClickListener(v -> getStartedClicked());

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    accountsProvider
        .getObservableCurrentAccount()
        .takeUntil(pause.happens())
        .subscribe(
            appAccount -> {
              currentAccount = appAccount;
              checkIfSignedIn();
            });
    checkIfSignedIn();
  }

  @Override
  public void onPause() {
    pause.onHappened();
    super.onPause();
  }

  private void checkIfSignedIn() {
    // If the user chooses Add Account from the account switcher dialog and adds an account,
    // onResume will be called and then onAccountSwitcherDialogFinished will be called. In that
    // case, our activity is already gone.
    if (getActivity() == null) {
      return;
    }

    if (accountsProvider.isSignedIn()) {
      afterSignIn();
      return;
    }

    if (accountsProvider.getAccountCount() > 0) {
      // If there is one or more accounts on the device, show the account switcher dialog on top of
      // this screen (without the user pressing "GET STARTED").
      showAccountSwitcherDialog();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_ACCOUNT_SWITCHER) {
      onAccountSwitcherDialogFinished();
    }
  }

  private void onAccountSwitcherDialogFinished() {
    showingAccountSwitcherDialog = false;
    checkIfSignedIn();
  }

  private void getStartedClicked() {
    if (accountsProvider.isSignedIn()) {
      // This shouldn't happen, but if it does, just make this screen go away.
      afterSignIn();
      return;
    }

    int accountCount = accountsProvider.getAccountCount();
    if (accountCount == 0) {
      // If there are no accounts on the device, show the add account dialog.
      accountsProvider.showAddAccountDialog(getActivity());
    } else if (accountCount > 0) {
      // If there is one or more accounts on the device, show the account switcher dialog.
      showAccountSwitcherDialog();
    }
  }

  private void showAccountSwitcherDialog() {
    if (!showingAccountSwitcherDialog) {
      showingAccountSwitcherDialog = true;
      accountsProvider.showAccountSwitcherDialog(this, REQUEST_CODE_ACCOUNT_SWITCHER);
    }
  }

  private void afterSignIn() {
    FragmentActivity activity = getActivity();
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

    // If there are any unclaimed experiments and we've never shown OldUserOptionPromptActivity,
    // show it now.
    if (AccountsUtils.getUnclaimedExperimentCount(activity) >= 1
        && !sharedPreferences.getBoolean(KEY_OLD_USER_OPTION_PROMPT_SHOWN, false)) {
      sharedPreferences.edit().putBoolean(KEY_OLD_USER_OPTION_PROMPT_SHOWN, true).apply();
      OldUserOptionPromptActivity.launch(activity, currentAccount);
    } else {
      // Otherwise, just go to MainActivity.
      activity.startActivity(new Intent(activity, MainActivity.class));
    }
    activity.finish();
  }
}
