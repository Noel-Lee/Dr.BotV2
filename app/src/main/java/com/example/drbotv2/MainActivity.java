package com.example.drbotv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button callSignUp, loginBtn;
    TextInputLayout teleHandle, password;
    String global_teleHandleStr;

    String doctorTeleHandle = "doctorAdmin123";
    String doctorPassword = "doctorPassword123";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        callSignUp = findViewById(R.id.toRegister);
        teleHandle = findViewById(R.id.login_teleHandle);
        password = findViewById(R.id.login_password);
        loginBtn = findViewById(R.id.loginBtn);


        callSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_signup);
            }
        });
    }

    public void loginBtn(View view) {

        FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://drbotv2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference reference = rootNode.getReference("Users");

        TextInputLayout loginTeleHandle, loginPassword;

        loginTeleHandle = findViewById(R.id.login_teleHandle);
        loginPassword = findViewById(R.id.login_password);
        String teleHandleStr = loginTeleHandle.getEditText().getText().toString();

        global_teleHandleStr = teleHandleStr;

        String passwordStr = loginPassword.getEditText().getText().toString();


        if (teleHandleStr.isEmpty() || passwordStr.isEmpty()) {
            Toast.makeText(MainActivity.this, "Required fields have been left blank", Toast.LENGTH_SHORT).show();

        } else if (teleHandleStr.charAt(0) == '@') {
            Toast.makeText(MainActivity.this, "Remove '@' from telegram handle", Toast.LENGTH_SHORT).show();
        } else if (teleHandleStr.equals(doctorTeleHandle) && passwordStr.equals(doctorPassword)) {
            toDoctorHome(view);
        } else {
            Query checkUser = reference.orderByChild("teleHandle").equalTo(teleHandleStr);

            checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String passwordFromDB = snapshot.child(teleHandleStr).child("password").getValue(String.class);
                        String number_reminders = String.valueOf(snapshot.child(teleHandleStr).child("Consumables").getChildrenCount());
                        String emailFromDB = snapshot.child(teleHandleStr).child("email").getValue(String.class);
                        String phoneNoFromDB = snapshot.child(teleHandleStr).child("phoneNo").getValue(String.class);

                        if (passwordFromDB.equals(passwordStr)) {
                            Toast.makeText(MainActivity.this, "LOGIN Successful", Toast.LENGTH_SHORT).show();
                            setContentView(R.layout.activity_home_screen);

                            TextView home_teleHandle_title = findViewById(R.id.home_teleHandle);
                            home_teleHandle_title.setText(teleHandleStr);

                            TextView number_reminders_tv = findViewById(R.id.number_reminders);
                            number_reminders_tv.setText(number_reminders);

                            toHomeScreen(view);

//                            TextInputLayout home_fullName = findViewById(R.id.full_name_profile);
//                            home_fullName.getEditText().setText(fullNameFromDB);
//                            TextInputLayout home_email = findViewById(R.id.email_profile);
//                            home_email.getEditText().setText(emailFromDB);
//                            TextInputLayout home_phoneNo = findViewById(R.id.phone_number_profile);
//                            home_phoneNo.getEditText().setText(phoneNoFromDB);


                        } else {
                            Toast.makeText(MainActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (teleHandleStr.equals(doctorTeleHandle) && passwordStr.equals(doctorPassword)) {
                            toDoctorHome(view);
                        } else {
                            Toast.makeText(MainActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    public void registerUserButton(View view) {

        FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://drbotv2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference reference = rootNode.getReference("Users");

        TextInputLayout regTeleHandle, regPassword, regPassword2, regGender, regBirthYear;

        regTeleHandle = findViewById(R.id.register_teleHandle);
        regPassword = findViewById(R.id.register_password);
        regPassword2 = findViewById(R.id.register_password2);
        regGender = findViewById(R.id.register_gender);
        regBirthYear = findViewById(R.id.register_birthYear);
        String teleHandleStr = regTeleHandle.getEditText().getText().toString();
        String passwordStr = regPassword.getEditText().getText().toString();
        String password2Str = regPassword2.getEditText().getText().toString();
        String genderStr = regGender.getEditText().getText().toString();
        String birthYearStr = regBirthYear.getEditText().getText().toString();

        try {
            int birthYearInt = Integer.parseInt(birthYearStr);
            if (birthYearInt > Calendar.getInstance().get(Calendar.YEAR)) {
                Toast.makeText(MainActivity.this, "Please input an appropriate year", Toast.LENGTH_SHORT).show();
            } else if (teleHandleStr.isEmpty() || passwordStr.isEmpty() || password2Str.isEmpty() || birthYearStr.isEmpty()) {
                Toast.makeText(MainActivity.this, "Required fields have been left blank", Toast.LENGTH_SHORT).show();
            } else if (teleHandleStr.charAt(0) == '@') {
                Toast.makeText(MainActivity.this, "Remove '@' from telegram handle", Toast.LENGTH_SHORT).show();
            } else if (!(genderStr.equals("Male") || genderStr.equals("Female") || genderStr.isEmpty())) {
                Toast.makeText(MainActivity.this, "Gender can only be 'Male', 'Female' or left blank", Toast.LENGTH_SHORT).show();
            } else if (!passwordStr.equals(password2Str)) {
                Toast.makeText(MainActivity.this, "Passwords have to be the same", Toast.LENGTH_SHORT).show();
            } else {
                UserHelperClass helperClass = new UserHelperClass(teleHandleStr, passwordStr, password2Str, genderStr, birthYearStr, "", "", "",  0, 0, 0, new ArrayList<ReminderRecordsHelperClass>(), new ArrayList<RecordConsumptionHelperClass>());
                reference.child(teleHandleStr).setValue(helperClass);
                setContentView(R.layout.activity_main);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(MainActivity.this, "Please input year in numbers", Toast.LENGTH_SHORT).show();
        }


    }

    public void toLoginButton(View view) {
        setContentView(R.layout.activity_main);
    }

    public void toRegisterButton(View view) {
        setContentView(R.layout.activity_signup);
    }

    public void toForgotPasswordButton(View view) {
        setContentView(R.layout.activity_forgot_password);
    }

    public void toRecordConsumptionButton(View view) {
        setContentView(R.layout.activity_record_consumption);
    }


    public void resetPasswordButton(View view) {
        FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://drbotv2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference reference = rootNode.getReference("Users");

        TextInputLayout forgot_password_TeleHandle, forgot_password_Password, forgot_password_Password2, forgot_password_Gender, forgot_password_BirthYear;

        forgot_password_TeleHandle = findViewById(R.id.forgot_password_teleHandle);
        forgot_password_Password = findViewById(R.id.forgot_password_password);
        forgot_password_Password2 = findViewById(R.id.forgot_password_password2);
        forgot_password_Gender = findViewById(R.id.forgot_password_gender);
        forgot_password_BirthYear = findViewById(R.id.forgot_password_birthYear);
        String teleHandleStr = forgot_password_TeleHandle.getEditText().getText().toString();
        String passwordStr = forgot_password_Password.getEditText().getText().toString();
        String password2Str = forgot_password_Password2.getEditText().getText().toString();
        String genderStr = forgot_password_Gender.getEditText().getText().toString();
        String birthYearStr = forgot_password_BirthYear.getEditText().getText().toString();

        global_teleHandleStr = teleHandleStr;

        try {
            int birthYearInt = Integer.parseInt(birthYearStr);
            if (birthYearInt > Calendar.getInstance().get(Calendar.YEAR)) {
                Toast.makeText(MainActivity.this, "Please input an appropriate year", Toast.LENGTH_SHORT).show();
            } else if (teleHandleStr.isEmpty() || passwordStr.isEmpty() || password2Str.isEmpty() || birthYearStr.isEmpty()) {
                Toast.makeText(MainActivity.this, "Required fields have been left blank", Toast.LENGTH_SHORT).show();
            } else if (!(genderStr.equals("Male") || genderStr.equals("Female") || genderStr.isEmpty())) {
                Toast.makeText(MainActivity.this, "Gender can only be 'Male', 'Female' or left blank", Toast.LENGTH_SHORT).show();
            } else if (!passwordStr.equals(password2Str)) {
                Toast.makeText(MainActivity.this, "Passwords have to be the same", Toast.LENGTH_SHORT).show();
            } else {
                Query checkUser = reference.orderByChild("teleHandle").equalTo(teleHandleStr);

                checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String birthYearFromDB = snapshot.child(teleHandleStr).child("birthYear").getValue(String.class);
//                            String fullNameFromDB = snapshot.child(teleHandleStr).child("fullName").getValue(String.class);
//                            String emailFromDB = snapshot.child(teleHandleStr).child("email").getValue(String.class);
//                            String phoneNoFromDB = snapshot.child(teleHandleStr).child("phoneNo").getValue(String.class);

                            if (birthYearFromDB.equals(birthYearStr)) {


//                                UserHelperClass helperClass = new UserHelperClass(teleHandleStr, passwordStr, password2Str, genderStr, birthYearStr, "", "", "", 0, 0, 0, new ArrayList<ReminderRecordsHelperClass>(), new ArrayList<RecordConsumptionHelperClass>());
                                reference.child(teleHandleStr).child("password").setValue(passwordStr);
                                reference.child(teleHandleStr).child("password2").setValue(password2Str);

                                Toast.makeText(MainActivity.this, "Password has been reset", Toast.LENGTH_SHORT).show();

                                toHomeScreen(view);

//                                setContentView(R.layout.activity_home_screen);
//                                TextView home_teleHandle_title = findViewById(R.id.home_teleHandle);
//                                home_teleHandle_title.setText(teleHandleStr);


                            } else {
                                Toast.makeText(MainActivity.this, "Incorrect Birth Year", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        } catch (NumberFormatException e) {
            Toast.makeText(MainActivity.this, "Please input year in numbers", Toast.LENGTH_SHORT).show();
        }


    }

    public void updateProfileButton(View view) {

        FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://drbotv2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference reference = rootNode.getReference("Users");


        String teleHandleStr = global_teleHandleStr;

        reference = reference.child(teleHandleStr);

        TextInputLayout home_fullName, home_email, home_phoneNo;

        home_fullName = findViewById(R.id.full_name_profile);
        home_email = findViewById(R.id.email_profile);
        home_phoneNo = findViewById(R.id.phone_number_profile);

        String home_fullNameStr = home_fullName.getEditText().getText().toString();
        String home_emailStr = home_email.getEditText().getText().toString();
        String home_phoneNoStr = home_phoneNo.getEditText().getText().toString();

        if (home_fullNameStr.isEmpty() || home_emailStr.isEmpty() || home_phoneNoStr.isEmpty()) {
            Toast.makeText(MainActivity.this, "Required fields have been left blank", Toast.LENGTH_SHORT).show();
        } else {
            reference.child("fullName").setValue(home_fullNameStr);
            reference.child("email").setValue(home_emailStr);
            reference.child("phoneNo").setValue(home_phoneNoStr);
        }
    }


    public void toConfigureRemindersButton(View view) {
        setContentView(R.layout.activity_configure_reminders);
    }

    public void toHomeScreen(View view) {
        setContentView(R.layout.activity_home_screen);

        FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://drbotv2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference reference = rootNode.getReference("Users");

        String teleHandleStr = global_teleHandleStr;

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

        Query checkUser = reference.child(teleHandleStr).child("Consumables").orderByChild("untilDate").startAt(timeStamp).endAt("3000-12-31");

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    TextView home_teleHandle_title = findViewById(R.id.home_teleHandle);
                    home_teleHandle_title.setText(global_teleHandleStr);

                    String number_reminders = String.valueOf(snapshot.getChildrenCount());

                    TextView number_reminders_tv = findViewById(R.id.number_reminders);
                    number_reminders_tv.setText(number_reminders);


                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        Query checkUser1 = reference.child(teleHandleStr);

        checkUser1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    int exp = ((Long) snapshot.child("exp").getValue()).intValue();
                    int level = exp / 100;

                    String levelStr = String.valueOf(level);

                    TextView number_reminders_tv = findViewById(R.id.level);
                    number_reminders_tv.setText("Level: " + levelStr);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    public void toRemindersOverviewButton(View view) {
        setContentView(R.layout.activity_reminder_overview);

        FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://drbotv2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference reference = rootNode.getReference("Users");

        String teleHandleStr = global_teleHandleStr;

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

        Query checkUser = reference.child(teleHandleStr).child("Consumables").orderByChild("untilDate").startAt(timeStamp).endAt("3000-12-31");

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    DataSnapshot data = snapshot;

                    String finalStr = "";
                    for (DataSnapshot ds : data.getChildren()) {
                        finalStr += ds.getKey() + "\n";
                        finalStr += "Duration in Days: " + ds.child("duration").getValue(String.class) + "\n";
                        finalStr += "Consume Until: " + ds.child("untilDate").getValue(String.class) + "\n";
                        finalStr += "Reminder Times: " + ds.child("reminder_times").getValue().toString() + "\n";
                        finalStr += "Remarks: " + ds.child("remarks").getValue(String.class) + "\n" + "\n";

                    }


                    TextView reminder1_tv = findViewById(R.id.reminder1);
                    reminder1_tv.setText(finalStr);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }



    public boolean check_time_format(ArrayList<String> times) {
        for (int i = 0; i < times.size(); i++) {
            if (!times.get(i).matches("^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$")) {
                return false;
            }
        }
        return true;
    }

    public boolean check_time_format(String time) {
        if (!time.matches("^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$")) {
            return false;
        } else {
            return true;
        }
    }

    public boolean check_date_format(String date) {
        if (!date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return false;
        } else {
            return true;
        }
    }

    public void saveReminderConfigButton(View view) {

        FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://drbotv2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference reference = rootNode.getReference("Users");


        String teleHandleStr = global_teleHandleStr;

        EditText consumable_name, consumable_duration, consumable_reminder_times, consumable_remarks;

        consumable_name = findViewById(R.id.consumable_config_name);
        consumable_duration = findViewById(R.id.consumable_config_duration);
        consumable_reminder_times = findViewById(R.id.consumable_config_reminder_times);
        consumable_remarks = findViewById(R.id.consumable_config_remarks);

        String consumable_nameStr = consumable_name.getText().toString();
        String consumable_durationStr = consumable_duration.getText().toString();
        String consumable_reminder_timesStr = consumable_reminder_times.getText().toString();
        String consumable_remarksStr = consumable_remarks.getText().toString();

        String reminder_times[] = consumable_reminder_timesStr.split(",");
        ArrayList<String> reminder_times_array = new ArrayList<>(Arrays.asList(reminder_times));



        try {
            int duration_int = Integer.parseInt(consumable_durationStr);
            if (duration_int <= 0) {
                Toast.makeText(MainActivity.this, "Please input an appropriate number of days", Toast.LENGTH_SHORT).show();
            } else if (consumable_nameStr.isEmpty() || consumable_durationStr.isEmpty() || consumable_reminder_timesStr.isEmpty()) {
                Toast.makeText(MainActivity.this, "Required fields have been left blank", Toast.LENGTH_SHORT).show();
            } else if (!check_time_format(reminder_times_array)) {
                Toast.makeText(MainActivity.this, "Input reminder times in hh:mm 24 hour format, separated by ','", Toast.LENGTH_SHORT).show();
            } else {


                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, duration_int);

                String consumable_untilDate = sdf.format(cal.getTime());

                ReminderRecordsHelperClass reminderRecordsHelperClass= new ReminderRecordsHelperClass(consumable_nameStr, consumable_durationStr, consumable_untilDate, consumable_reminder_timesStr, consumable_remarksStr);
                reference.child(teleHandleStr).child("Consumables").child(consumable_nameStr).setValue(reminderRecordsHelperClass);
                Toast.makeText(MainActivity.this, "Update Successful", Toast.LENGTH_SHORT).show();



            }
        } catch (NumberFormatException e) {
            Toast.makeText(MainActivity.this, "Please input duration in integer format", Toast.LENGTH_SHORT).show();
        }

    }

    public void deleteReminderConfigButton(View view) {

        FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://drbotv2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference reference = rootNode.getReference("Users");

        String teleHandleStr = global_teleHandleStr;
        EditText consumable_name = findViewById(R.id.consumable_config_name);
        String consumable_nameStr = consumable_name.getText().toString();

        if (consumable_nameStr.isEmpty()) {
            Toast.makeText(MainActivity.this, "Required fields have been left blank", Toast.LENGTH_SHORT).show();
        } else {
            Query checkUser = reference.child(teleHandleStr).child("Consumables").orderByChild("consumable_name").equalTo(consumable_nameStr);

            checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        reference.child(teleHandleStr).child("Consumables").child(consumable_nameStr).setValue(null);
                        Toast.makeText(MainActivity.this, "Consumable reminder has been deleted", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(MainActivity.this, "Consumable does not exist", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public long timeDifference(String time1, String time2) {
        long difference = 0;
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            Date date1 = format.parse(time1);
            Date date2 = format.parse(time2);
            difference = date2.getTime() - date1.getTime();

        } catch (Exception e) {
            System.out.println(e);
            difference = 10000000;
        }
        return Math.abs(difference);
    }

    public boolean lessThanHour(String time1, String time2) {
        return timeDifference(time1, time2) <= 3600000;
    }



    public void saveRecordConsumptionButton(View view) {

        FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://drbotv2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference reference = rootNode.getReference("Users");


        String teleHandleStr = global_teleHandleStr;

        EditText record_name, record_date, record_time, record_remarks;

        record_name = findViewById(R.id.record_consumption_name);
        record_date = findViewById(R.id.record_consumption_date);
        record_time = findViewById(R.id.record_consumption_time);
        record_remarks = findViewById(R.id.record_consumption_remarks);

        String record_nameStr = record_name.getText().toString();
        String record_dateStr = record_date.getText().toString();
        String record_timeStr = record_time.getText().toString();
        String record_remarksStr = record_remarks.getText().toString();

        if (record_nameStr.isEmpty() || record_dateStr.isEmpty() || record_timeStr.isEmpty()) {
            Toast.makeText(MainActivity.this, "Required fields have been left blank", Toast.LENGTH_SHORT).show();
        } else if (!check_date_format(record_dateStr)) {
            Toast.makeText(MainActivity.this, "Please input date in 'YYYY-MM-DD' format", Toast.LENGTH_SHORT).show();
        } else if (!check_time_format(record_timeStr)) {
            Toast.makeText(MainActivity.this, "Please input time in HH:MM 24 hour format", Toast.LENGTH_SHORT).show();
        } else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date enteredDate = sdf.parse(record_dateStr);

                if (enteredDate.after(new Date())) {
                    Toast.makeText(MainActivity.this, "Please input an appropriate date", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Please input an appropriate date in 'YYYY-MM-DD' format", Toast.LENGTH_SHORT).show();
            }

            Query checkUser = reference.child(global_teleHandleStr).child("Consumables").orderByChild("consumable_name").equalTo(record_nameStr);

            checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        RecordConsumptionHelperClass recordConsumptionHelperClass = new RecordConsumptionHelperClass(record_nameStr + "," + record_dateStr + "," + record_timeStr);
                        reference.child(global_teleHandleStr).child("Consumption Data").child(record_nameStr + "," + record_dateStr + "," + record_timeStr).setValue(recordConsumptionHelperClass);

                    } else {
                        Toast.makeText(MainActivity.this, "Consumable does not exist", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


//            String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            Query checkUser1 = reference.child(teleHandleStr);

            checkUser1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        int currentEXP = ((Long) snapshot.child("exp").getValue()).intValue();
                        int currentTimelyConsumption = ((Long) snapshot.child("timelyConsumption").getValue()).intValue();
                        int currentTotalConsumption = ((Long) snapshot.child("totalConsumption").getValue()).intValue();


                        String timings = snapshot.child("Consumables").child(record_nameStr).child("reminder_times").getValue(String.class);
                        String reminder_times[] = timings.split(",");
                        ArrayList<String> reminder_times_array = new ArrayList<>(Arrays.asList(reminder_times));


                        for (String time : reminder_times_array) {

                            if (lessThanHour(time, record_timeStr)) {
                                reference.child(global_teleHandleStr).child("exp").setValue(currentEXP + 10);
                                reference.child(global_teleHandleStr).child("timelyConsumption").setValue(currentTimelyConsumption + 1);
                                break;
                            }
                        }

                        reference.child(global_teleHandleStr).child("totalConsumption").setValue(currentTotalConsumption + 1);
                        Toast.makeText(MainActivity.this, "Consumption record successfully saved", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(MainActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }

    }

    public void toUserProfileButtonByUser(View view) {
        setContentView(R.layout.activity_user_profile);


        FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://drbotv2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference reference = rootNode.getReference("Users");

        String teleHandleStr = global_teleHandleStr;

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

        Query checkUser = reference.child(teleHandleStr).child("Consumables").orderByChild("untilDate").startAt(timeStamp).endAt("3000-12-31");

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    DataSnapshot data = snapshot;

                    String finalStr = "";
                    for (DataSnapshot ds : data.getChildren()) {
                        finalStr += ds.getKey() + "\n";
                        finalStr += "Duration in Days: " + ds.child("duration").getValue(String.class) + "\n";
                        finalStr += "Consume Until: " + ds.child("untilDate").getValue(String.class) + "\n";
                        finalStr += "Reminder Times: " + ds.child("reminder_times").getValue().toString() + "\n";
                        finalStr += "Remarks: " + ds.child("remarks").getValue(String.class) + "\n" + "\n";

                    }


                    TextView reminder1_tv = findViewById(R.id.user_profile_consumable_regime);
                    reminder1_tv.setText(finalStr);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        Query checkUser1 = reference.child(teleHandleStr);

        checkUser1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    DataSnapshot ds = snapshot;

                    String finalStr = "";

                    finalStr += "Full Name: " + ds.child("fullName").getValue(String.class) + "\n";
                    finalStr += "Telegram Handle: " + ds.child("teleHandle").getValue(String.class) + "\n";
                    finalStr += "Gender: " + ds.child("gender").getValue().toString() + "\n";
                    finalStr += "Birth Year: " + ds.child("birthYear").getValue().toString() + "\n";
                    finalStr += "Phone Number: " + ds.child("phoneNo").getValue().toString() + "\n";
                    finalStr += "Email: " + ds.child("email").getValue().toString() + "\n";

                    int currentEXP = ((Long) ds.child("exp").getValue()).intValue();
                    int level = currentEXP / 100;
                    int currentTimelyConsumption = ((Long) ds.child("timelyConsumption").getValue()).intValue();
                    int currentTotalConsumption = ((Long) ds.child("totalConsumption").getValue()).intValue();

                    finalStr += "Timely Consumption: " + String.valueOf(currentTimelyConsumption) + "\n";
                    finalStr += "Total Consumption: " + String.valueOf(currentTotalConsumption) + "\n" + "\n";




                    TextView reminder1_tv = findViewById(R.id.user_profile_details);
                    reminder1_tv.setText(finalStr);

                    TextView teleHandle_tv = findViewById(R.id.user_profile_teleHandle);
                    teleHandle_tv.setText(global_teleHandleStr);


                    String levelStr = String.valueOf(level);
                    TextView level_tv = findViewById(R.id.user_profile_level);
                    level_tv.setText("Level: " + levelStr);

                    int timelyConsumptionRate = 100 * currentTimelyConsumption / currentTotalConsumption;
                    String timelyConsumptionRateStr = String.valueOf(timelyConsumptionRate) + "%";
                    TextView timelyConsumptionRate_tv = findViewById(R.id.timely_consumption_rate);
                    timelyConsumptionRate_tv.setText(timelyConsumptionRateStr);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


    }

    public void toDoctorHome(View view) {
        setContentView(R.layout.activity_home_screen_doctor);
        global_teleHandleStr = doctorTeleHandle;

        FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://drbotv2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference reference = rootNode.getReference("Users");

        Query checkUser = reference;

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    long number_patientsLong = snapshot.getChildrenCount() - 1;
                    String number_patientsStr = String.valueOf(number_patientsLong);

                    TextView number_patients_tv = findViewById(R.id.number_patients);
                    number_patients_tv.setText(number_patientsStr);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


    }

    public void toUserProfileDoctor(View view) {
        setContentView(R.layout.activity_user_profile_doctor);


        FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://drbotv2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference reference = rootNode.getReference("Users");

        String teleHandleStr = global_teleHandleStr;

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

        Query checkUser = reference.child(teleHandleStr).child("Consumables").orderByChild("untilDate").startAt(timeStamp).endAt("3000-12-31");

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    DataSnapshot data = snapshot;

                    String finalStr = "";
                    for (DataSnapshot ds : data.getChildren()) {
                        finalStr += ds.getKey() + "\n";
                        finalStr += "Duration in Days: " + ds.child("duration").getValue(String.class) + "\n";
                        finalStr += "Consume Until: " + ds.child("untilDate").getValue(String.class) + "\n";
                        finalStr += "Reminder Times: " + ds.child("reminder_times").getValue().toString() + "\n";
                        finalStr += "Remarks: " + ds.child("remarks").getValue(String.class) + "\n" + "\n";

                    }


                    TextView reminder1_tv = findViewById(R.id.user_profile_consumable_regime);
                    reminder1_tv.setText(finalStr);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        Query checkUser1 = reference.child(teleHandleStr);

        checkUser1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    DataSnapshot ds = snapshot;

                    String finalStr = "";

                    finalStr += "Full Name: " + ds.child("fullName").getValue(String.class) + "\n";
                    finalStr += "Telegram Handle: " + ds.child("teleHandle").getValue(String.class) + "\n";
                    finalStr += "Gender: " + ds.child("gender").getValue().toString() + "\n";
                    finalStr += "Birth Year: " + ds.child("birthYear").getValue().toString() + "\n";
                    finalStr += "Phone Number: " + ds.child("phoneNo").getValue().toString() + "\n";
                    finalStr += "Email: " + ds.child("email").getValue().toString() + "\n";

                    int currentEXP = ((Long) ds.child("exp").getValue()).intValue();
                    int level = currentEXP / 100;
                    int currentTimelyConsumption = ((Long) ds.child("timelyConsumption").getValue()).intValue();
                    int currentTotalConsumption = ((Long) ds.child("totalConsumption").getValue()).intValue();

                    finalStr += "Timely Consumption: " + String.valueOf(currentTimelyConsumption) + "\n";
                    finalStr += "Total Consumption: " + String.valueOf(currentTotalConsumption) + "\n" + "\n";




                    TextView reminder1_tv = findViewById(R.id.user_profile_details);
                    reminder1_tv.setText(finalStr);

                    TextView teleHandle_tv = findViewById(R.id.user_profile_teleHandle);
                    teleHandle_tv.setText(global_teleHandleStr);


                    String levelStr = String.valueOf(level);
                    TextView level_tv = findViewById(R.id.user_profile_level);
                    level_tv.setText("Level: " + levelStr);

                    int timelyConsumptionRate;

                    if (currentTotalConsumption <= 0) {
                        timelyConsumptionRate = 100;
                    } else {
                        timelyConsumptionRate = 100 * currentTimelyConsumption / currentTotalConsumption;
                    }

                    String timelyConsumptionRateStr = String.valueOf(timelyConsumptionRate) + "%";
                    TextView timelyConsumptionRate_tv = findViewById(R.id.timely_consumption_rate);
                    timelyConsumptionRate_tv.setText(timelyConsumptionRateStr);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    public void doctorSearchButton(View view) {


        FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://drbotv2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference reference = rootNode.getReference("Users");




        TextInputLayout local_teleHandle;

        local_teleHandle = findViewById(R.id.doctor_home_title);


        String local_teleHandleStr = local_teleHandle.getEditText().getText().toString();

        global_teleHandleStr = local_teleHandleStr;


        if (local_teleHandleStr.isEmpty()) {
            Toast.makeText(MainActivity.this, "Required fields have been left blank", Toast.LENGTH_SHORT).show();
        } else if (local_teleHandleStr.charAt(0) == '@') {
            Toast.makeText(MainActivity.this, "Please input Telegram Handle without the '@'", Toast.LENGTH_SHORT).show();
        }

        toUserProfileDoctor(view);

    }

    public void toDoctorConfigureRemindersButton(View view) {
        setContentView(R.layout.activity_configure_reminders_doctor);
    }

}