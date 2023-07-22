package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

public class AudioPlayer {

    private final MqttClient mqttClient;
    private final AudioTrack audioTrack;
    private final AudioRecord audioRecord;
    private final ArrayBlockingQueue<byte[]> audioQueue = new ArrayBlockingQueue<>(50); // Adjust size as needed
    private final Context context;
    private boolean recording = true;

    @SuppressLint("MissingPermission")
    public AudioPlayer(Context context) throws MqttException {
        this.context = context;

        // MQTT setup
        String serverUri = "tcp://172.20.10.3:1883";
        String clientId = "AndroidClient"; // change as needed
        mqttClient = new MqttClient(serverUri, clientId, new MemoryPersistence());

        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true); // change as needed

        mqttClient.connect(connOpts);

        // AudioTrack setup
        int sampleRate = 16000; // change as needed  16000
        int bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_8BIT
        );

        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_8BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
        );

        // AudioRecord setup
        int inputBufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_8BIT
        );

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_8BIT,
                inputBufferSize
        );

        // MQTT callbacks
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                // handle connection lost
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // add the audio data to the queue
                audioQueue.put(message.getPayload());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // handle delivery complete
            }
        });

        // start the audio
        audioTrack.play();

        // subscribe to the topics
        mqttClient.subscribe("ESP32_RECVER");

        // Start a new thread to read from the queue and write to the AudioTrack
        new Thread(() -> {
            while (true) {
                try {
                    byte[] audioData = audioQueue.take();
                    audioTrack.write(audioData, 0, audioData.length);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Request audio focus
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        AudioFocusRequest audioFocusRequest = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(focusChange -> {
                        switch (focusChange) {
                            case AudioManager.AUDIOFOCUS_GAIN:
                                // Resume playback or lower volume
                                break;
                            case AudioManager.AUDIOFOCUS_LOSS:
                                // Stop playback or lower volume
                                break;
                            default:
                                // Handle other cases
                                break;
                        }
                    })
                    .build();
        }
        int focusRequestResult = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            focusRequestResult = audioManager.requestAudioFocus(audioFocusRequest);
        }
        if (focusRequestResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Audio focus was granted
        } else {
            // Audio focus was denied
        }
    }

    public void startMicrophone() {
        recording = true;
        int inputBufferSize = AudioRecord.getMinBufferSize(
                16000, // change as needed
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_8BIT
        );

        // Start a new thread to read from the AudioRecord and publish to the MQTT
        new Thread(() -> {
            byte[] buffer = new byte[inputBufferSize];
            audioRecord.startRecording();
            while (recording) {
                int read = audioRecord.read(buffer, 0, inputBufferSize);
                if (read > 0) {
                    // Adjust the payload length to match the ESP-32 payload length
                    int payloadLength = 1024; // Set the desired payload length (in bits)
                    int requiredBytes = payloadLength / 8; // Convert payload length to bytes

                    // Apply audio processing to improve sound quality
                    byte[] processedBuffer = applyAudioProcessing(buffer, read);

                    // Split the processed audio into multiple payloads
                    int numPayloads = (int) Math.ceil((double) processedBuffer.length / requiredBytes);
                    for (int i = 0; i < numPayloads; i++) {
                        int startIndex = i * requiredBytes;
                        int endIndex = Math.min(startIndex + requiredBytes, processedBuffer.length);
                        byte[] payload = Arrays.copyOfRange(processedBuffer, startIndex, endIndex);

                        try {
                            mqttClient.publish("ESP32_SENDER", payload, 0, false);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            audioRecord.stop();
        }).start();

    }

    private byte[] applyAudioProcessing(byte[] buffer, int length) {
        // Apply your audio processing techniques here
        // Example: Perform volume normalization
        double maxAmplitude = 0;
        for (int i = 0; i < length; i++) {
            byte sample = buffer[i];
            double amplitude = Math.abs(sample / 128.0);
            maxAmplitude = Math.max(maxAmplitude, amplitude);
        }
        double scaleFactor = 1.0;
        if (maxAmplitude > 0) {
            scaleFactor = 1.0 / maxAmplitude;
        }

        byte[] processedBuffer = new byte[length];
        for (int i = 0; i < length; i++) {
            byte sample = buffer[i];
            int processedSample = (int) (sample * scaleFactor);
            processedBuffer[i] = (byte) processedSample;
        }

        return processedBuffer;
    }

    public void stopMicrophone() {
        recording = false;
    }

    public void stop() {
        // Signal the recording thread to stop
        recording = false;

        // Stop and release the AudioRecord
        audioRecord.stop();
        audioRecord.release();

        // Stop the AudioTrack
        if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
        }

        // Release the resources used by the AudioTrack
        audioTrack.release();

        // Disconnect the MQTT client
        if (mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
