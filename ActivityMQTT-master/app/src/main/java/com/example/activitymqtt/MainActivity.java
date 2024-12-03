package com.example.activitymqtt;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MQTTApp";
    private MqttAndroidClient mqttClient;
    private EditText etTopic, etMessage;
    private Button btnSendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        etTopic = findViewById(R.id.et_topic);
        etMessage = findViewById(R.id.et_message);
        btnSendMessage = findViewById(R.id.btn_send_message);


        setupMQTTClient();


        btnSendMessage.setOnClickListener(v -> {
            String topic = etTopic.getText().toString().trim();
            String message = etMessage.getText().toString().trim();

            if (topic.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Debe ingresar el topic y el mensaje", Toast.LENGTH_SHORT).show();
                return;
            }

            publishMessage(topic, message);
        });
    }

    // Configurar cliente MQTT
    private void setupMQTTClient() {
        String brokerUrl = "tcp://test.mosquitto.org:1883"; // URL del broker MQTT
        String clientId = MqttClient.generateClientId(); // Generar un ID único para el cliente

        mqttClient = new MqttAndroidClient(getApplicationContext(), brokerUrl, clientId);

        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Conexión exitosa al broker MQTT");
                    Toast.makeText(MainActivity.this, "Conexión exitosa al broker", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Error al conectar al broker", exception);
                    Toast.makeText(MainActivity.this, "Error al conectar al broker", Toast.LENGTH_SHORT).show();
                }
            });

            // Configurar callback para recibir mensajes
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "Conexión perdida con el broker", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d(TAG, "Mensaje recibido del topic " + topic + ": " + message.toString());
                    Toast.makeText(MainActivity.this, "Mensaje recibido: " + message.toString(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Mensaje entregado correctamente");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // Publicar un mensaje en un topic
    private void publishMessage(String topic, String message) {
        try {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(message.getBytes());
            mqttMessage.setQos(0); // Calidad del servicio (QoS) 0

            mqttClient.publish(topic, mqttMessage);
            Log.d(TAG, "Mensaje publicado: " + message);
            Toast.makeText(this, "Mensaje enviado", Toast.LENGTH_SHORT).show();

        } catch (MqttException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Desconectar el cliente MQTT al cerrar la aplicación
        if (mqttClient != null) {
            try {
                mqttClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}