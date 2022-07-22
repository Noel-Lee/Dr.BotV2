package com.example.drbotv2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class activity_signup extends AppCompatActivity {

    TextInputLayout regTeleHandle, regPassword, regPassword2, regGender, regBirthYear;
    Button regBtn;

    FirebaseDatabase rootNode;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        regTeleHandle = findViewById(R.id.register_teleHandle);
        regPassword = findViewById(R.id.register_password);
        regPassword2 = findViewById(R.id.register_password2);
        regGender = findViewById(R.id.register_gender);
        regBirthYear = findViewById(R.id.register_birthYear);
        regBtn = findViewById(R.id.regBtn);
//
//
//        regBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                rootNode = FirebaseDatabase.getInstance("https://drbotv2-default-rtdb.asia-southeast1.firebasedatabase.app/");
//                reference = rootNode.getReference("Users");
//
//                String teleHandleStr = regTeleHandle.getEditText().getText().toString();
//                String passwordStr = regPassword.getEditText().getText().toString();
//                String password2Str = regPassword2.getEditText().getText().toString();
//                String genderStr = regGender.getEditText().getText().toString();
//                String birthYearStr = regBirthYear.getEditText().getText().toString();
//
//                UserHelperClass helperClass = new UserHelperClass(teleHandleStr, passwordStr, password2Str, genderStr, birthYearStr);
//
//                reference.child(teleHandleStr).setValue(helperClass);
//
//                setContentView(R.layout.activity_main);
//
//            }
//        });

    }

    public Boolean validateTeleHandle() {

        String val = regTeleHandle.getEditText().toString();

        if (val.isEmpty()) {
            regTeleHandle.setError("Field cannot be empty");
            return false;

        } else if (val.charAt(0) == '@') {
            regTeleHandle.setError("Remove '@' from your telegram handle");
            return false;

        } else {
            regTeleHandle.setError(null);
            return true;

        }
    }

    public Boolean validatePassword() {
        String val = regPassword.getEditText().toString();

        if (val.isEmpty()) {
            regPassword.setError("Field cannot be empty");
            return false;

        } else {
            regPassword.setError(null);
            return true;

        }
    }

    public Boolean validatePassword2() {
        String val = regPassword2.getEditText().toString();

        if (val.isEmpty()) {
            regPassword2.setError("Field cannot be empty");
            return false;

        } else if (!val.equals(regPassword.getEditText().toString())) {
            regPassword2.setError("Password not the same");
            return false;

        } else {
            regPassword2.setError(null);
            return true;

        }
    }

    public Boolean validateGender() {
        String val = regGender.getEditText().toString();

       if (!(val.equals("Male") || val.equals("Female") || val.isEmpty())){
            regGender.setError("Enter gender in the appropriate format");
            return false;

       } else {
            regGender.setError(null);
            return true;
       }
    }

    public Boolean validateBirthYear() {
        String val = regBirthYear.getEditText().toString();

        if (val.isEmpty()) {
            regBirthYear.setError("Field cannot be empty");
            return false;

        } else {
            try {
                int birthYearInt = Integer.parseInt(val);
                if (birthYearInt > Calendar.getInstance().get(Calendar.YEAR)) {
                    regBirthYear.setError("Input an appropriate year");
                    return false;
                } else {
                    regBirthYear.setError(null);
                    return true;
                }
            } catch (NumberFormatException e) {
                regBirthYear.setError("Input year in numbers");
                return false;
            }

        }
    }



//    public void registerUserButton(View view) {
//
//        rootNode = FirebaseDatabase.getInstance("https://drbotv2-default-rtdb.asia-southeast1.firebasedatabase.app/");
//        reference = rootNode.getReference("Users");
//
//
//        if (!validateTeleHandle() | !validatePassword() | !validatePassword2() | !validateGender() | !validateBirthYear()) {
//            return;
//        }
//
//        String teleHandleStr = regTeleHandle.getEditText().toString();
//        String passwordStr = regPassword.getEditText().toString();
//        String password2Str = regPassword2.getEditText().toString();
//        String genderStr = regGender.getEditText().toString();
//        String birthYearStr = regBirthYear.getEditText().toString();
//
//        UserHelperClass helperClass = new UserHelperClass(teleHandleStr, passwordStr, password2Str, genderStr, birthYearStr);
//        reference.setValue(helperClass);
//
//
//    }

}