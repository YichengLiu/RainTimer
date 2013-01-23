package me.gods.raintimer;

import java.util.Locale;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class TimerFragment extends Fragment {
    private static final String[] m={"A型","B型","O型","AB型","其他"};

    private Spinner eventSpinner;
    private ArrayAdapter<String> adapter;
    private TextView timerText;
    private Button switcherButton;

    private enum State {
        reset,
        running,
        pause,
        stop
    };

    private State state;
    private long startTime;
    private long offsetTime;
    private TimerThread timerThread;

    final Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what) {
            case 1:
                long minus = System.currentTimeMillis() - startTime;
                timerText.setText(millisToTime(offsetTime + minus));

                break;
            default:
                break;
            }

            super.handleMessage(msg);
        }
    };

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.state = State.reset;
    }

    @Override
    public void onResume () {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_timer, container, false);

        eventSpinner = (Spinner)v.findViewById(R.id.event_spinner);
        adapter = new ArrayAdapter<String>(this.getActivity(), R.layout.spinner_text, m);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventSpinner.setAdapter(adapter);

        timerText = (TextView)v.findViewById(R.id.timer_text);
        timerText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (state == State.running) {
                    state = State.pause;
                    try {
                        timerThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    offsetTime += System.currentTimeMillis() - startTime;
                    startTime = System.currentTimeMillis();
                } else if (state == State.pause) {
                    state = State.running;
                    startTime = System.currentTimeMillis();
                    timerThread = new TimerThread();
                    timerThread.start();
                }
            }
        });

        switcherButton = (Button)v.findViewById(R.id.timer_switcher);
        switcherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(state) {
                    case reset:
                        state = State.running;
                        startTime = System.currentTimeMillis();
                        timerThread = new TimerThread();
                        timerThread.start();

                        break;
                    case running:
                    case pause:
                        state = State.stop;
                        try {
                            timerThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        break;
                    case stop:
                        state = State.reset;
                        timerText.setText("0'00\"00");
                        offsetTime = 0;

                        break;
                    default:
                        break;
                }

                updateButton();
            }
        });

        updateButton();

        return v;
    }

    private String millisToTime(long millis) {
        long millisecond = (millis / 10) % 100;
        long second = (millis / 1000) % 60;
        long minute = (millis / 1000 / 60) % 60;

        return String.format(Locale.getDefault(), "%d'%02d\"%02d", minute, second, millisecond);
    }

    public void updateButton() {
        switch(state) {
            case reset:
                switcherButton.setText("Start");
                break;
            case running:
                switcherButton.setText("Stop");
                break;
            case stop:
                switcherButton.setText("Reset");
                break;
            default:
                break;
        }
    }

    public class TimerThread extends Thread {      // thread
        @Override
        public void run(){
            while(state == TimerFragment.State.running){
                try{
                    Thread.sleep(10);
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }catch (Exception e) {

                }
            }
        }
    }
}
