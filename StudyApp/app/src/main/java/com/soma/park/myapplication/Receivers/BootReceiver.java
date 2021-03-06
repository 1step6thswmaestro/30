package com.soma.park.myapplication.Receivers;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.soma.park.myapplication.Elements.ReferenceMonitor;
import com.soma.park.myapplication.Services.ScreenService;

import java.util.Calendar;

/**
 * Created by PARK on 15. 10. 19..
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    private ReferenceMonitor referenceMonitor = ReferenceMonitor.getInstance();
    SharedPreferences pref;
    private AlarmManager alarmManager;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            pref = context.getSharedPreferences("pref", Activity.MODE_PRIVATE);
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Log.d(TAG, "핸드폰 부팅 완료");
            Log.d(TAG, "alarmstate: " + String.valueOf(pref.getBoolean("alarmstate", false)));
            Log.d(TAG, "lockstate: " + String.valueOf(pref.getBoolean("lockstate", false)));

            if(pref.getBoolean("lockstate", false)) {
                referenceMonitor.setStudymode();
                Intent intent1 = new Intent(context, ScreenService.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(intent1);

                Calendar calendar2 = Calendar.getInstance();
                calendar2.set(Calendar.HOUR_OF_DAY, pref.getInt("bootendhour", 0));
                calendar2.set(Calendar.MINUTE, pref.getInt("bootendmin", 0));
                calendar2.set(Calendar.SECOND, 0);
                Intent intent2 = new Intent(context, NowStopReceiver.class);
                PendingIntent pIntent2 = PendingIntent.getBroadcast(context, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(), pIntent2);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(), pIntent2);
                }

            }

            if(pref.getBoolean("alarmstate", false)) {
                referenceMonitor.setStudymode();
                Intent newintent = new Intent(context, ScreenService.class);
                newintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(newintent);

                int size = pref.getInt("size", 0);
                for(int position = 1; position <= size; position++) {
                    boolean[] week = { false, pref.getBoolean("sun"+position, false), pref.getBoolean("mon"+position, false),
                            pref.getBoolean("tue" + position, false), pref.getBoolean("wed" + position, false),
                            pref.getBoolean("thu" + position, false), pref.getBoolean("fri" + position, false),
                            pref.getBoolean("sat" + position, false) };    // sunday=1 이라서 0의 자리에는 아무 값이나 넣었음
                    boolean checkweek = pref.getBoolean("checkweek"+position, false);
                    long oneday = 24 * 60 * 60 * 1000;  // 24시간

                    Log.d(TAG, "position: " + String.valueOf(position-1));
                    Log.d(TAG, "pref_position: " + String.valueOf(position));

                    Intent intent1 = new Intent(context, AlarmStartReceiver.class);
                    intent1.putExtra("weekday", week);
                    intent1.putExtra("checkweek", checkweek);
                    intent1.putExtra("position", position);

                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.set(Calendar.HOUR_OF_DAY, pref.getInt("startHour" + position, 0));
                    calendar1.set(Calendar.MINUTE, pref.getInt("startMin" + position, 0));
                    calendar1.set(Calendar.SECOND, 0);

                    Calendar currentCal = Calendar.getInstance();
                    currentCal.set(Calendar.SECOND, 0);
                    long current = currentCal.getTimeInMillis();

                    if(calendar1.getTimeInMillis() < current) {     // 현재 시간 이전이면 (즉, 내일부터이면)
                        int day = calendar1.get(Calendar.DATE);
                        calendar1.set(Calendar.DATE, day+1);
                    }
                    Log.d(TAG, String.valueOf(calendar1.getTime()));
                    PendingIntent pIntent1 = PendingIntent.getBroadcast(context, position, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {      //api 19 이상
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar1.getTimeInMillis(), pIntent1);
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar1.getTimeInMillis(), pIntent1);
                    }

                    Intent intent2 = new Intent(context, AlarmStopReceiver.class);
                    intent2.putExtra("weekday", week);
                    intent2.putExtra("checkweek", checkweek);
                    intent2.putExtra("position", position);

                    Calendar calendar2 = Calendar.getInstance();
                    calendar2.set(Calendar.HOUR_OF_DAY, pref.getInt("endHour" + position, 0));
                    calendar2.set(Calendar.MINUTE, pref.getInt("endMin" + position, 0));
                    calendar2.set(Calendar.SECOND, 0);

                    if(calendar2.getTimeInMillis() < current) {    // 현재 시간 이전이면 (즉, 내일부터이면)
                        int day = calendar2.get(Calendar.DATE);
                        calendar2.set(Calendar.DATE, day+1);
                    }
                    Log.d(TAG, String.valueOf(calendar2.getTime()));
                    PendingIntent pIntent2 = PendingIntent.getBroadcast(context, position, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(), pIntent2);
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(), pIntent2);
                    }
                }
            }
        }
    }
}