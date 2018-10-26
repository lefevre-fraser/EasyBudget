package com.budget.lefevre.easybudget;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

/**
 * Created by mp3le on 3/31/2018.
 */

// class for handling creation of menu items
public class MenuOptions extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private static final int DELETE_ACCOUNT = 100;

    FirebaseUser user;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // determine if user is signed inn and determine instruction to be executed
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            switch (item.getItemId()) {
                case R.id.OM_signIn: {
                    new ToastMessages(this, "You are already signed in");
                    break;
                }
                case R.id.OM_SignOut:
                    signOut();
                    return true;
                case R.id.OM_deleteAccount:
                    deleteAccount();
                    return true;
                case R.id.OM_budgetPanel:
                    switchToBudgeting();
                    return true;
                case R.id.OM_createNewBudget:
                    createNewBudget();
                    return true;
                case R.id.OM_accountsList:
                    switchToAccountsList();
                    return true;
                case R.id.OM_addNewAccount:
                    addNewAccount();
                    return true;
                case R.id.OM_transferMoney:
                    transferMoneys();
                    return true;
                default:
                    break;
            }
        } else if (item.getItemId() == R.id.OM_signIn) {
            signIn();
        } else {
            new ToastMessages(this, "Not currently Signed In");
        }

        return super.onOptionsItemSelected(item);
    }

    // calls to other classes for moving around app
    public void addNewAccount() {
        Intent intent = new Intent(this, AddNewAccount.class);
        startActivity(intent);
    }

    public void transferMoneys() {
        Intent intent = new Intent(this, TransferMoneyActivity.class);
        startActivity(intent);
    }

    public void signIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        new ToastMessages(MenuOptions.this, "Successfully signed out!");

                        Intent intent = new Intent(MenuOptions.this, AccountsDisplay.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
    }

    public void deleteAccount() {
        Intent intent = new Intent(this, ConfirmationPopUp.class);

        intent.putExtra("confirm/Deny", "delete your Easy Budget Account");
        startActivityForResult(intent, DELETE_ACCOUNT);
    }

    public void switchToBudgeting() {
        Intent intent = new Intent(this, BudgetPanel.class);
        startActivity(intent);
    }

    public void switchToAccountsList() {
        Intent intent = new Intent(this, AccountsDisplay.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void createNewBudget() {
        Intent intent = new Intent(this, CreateNewBudget.class);
        startActivity(intent);
    }

    // on result handling
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // user wants to delete their account
        if (requestCode == DELETE_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                {
                    // delete user data
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference reference = database.getReference().child(user.getUid());
                    reference.removeValue();
                    new ToastMessages(this, "Deleted data for your account");
                }

                // delete user account from FireBase
                AuthUI.getInstance()
                        .delete(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // ...
                                new ToastMessages(MenuOptions.this, "Deleted your Easy Budget Account");

                                Intent intent = new Intent(MenuOptions.this, AccountsDisplay.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }

        // user is signing in
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                user = FirebaseAuth.getInstance().getCurrentUser();

                new ToastMessages(this, "Successfully signed in!");

                Intent intent = new Intent(this, AccountsDisplay.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                // Sign in failed, check response for error code
                // ...
            }
        }
    }
}
