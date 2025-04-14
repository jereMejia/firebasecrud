package com.example.firebasecrud;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText nombre, apellido, correo;
    TextView fecha;
    LinearLayout datePickerContainer;
    ImageView imageView;
    ImageButton btnFoto;
    Button btnGuardar, btnRegistros;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;

    private static final int COD_SEL_IMAGE = 300;
    private static final int COD_TAKE_PHOTO = 400;
    private Uri image_url;
    private Uri photoUri;

    String idd;

    Bitmap bitmapSelected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String id = getIntent().getStringExtra("id_persona");

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference("personas");

        nombre = findViewById(R.id.txtNombre);
        apellido = findViewById(R.id.txtApellido);
        correo = findViewById(R.id.txtCorreo);
        fecha = findViewById(R.id.txtFecha);
        datePickerContainer = findViewById(R.id.datePickerContainer);
        imageView = findViewById(R.id.imageView);
        btnFoto = findViewById(R.id.btnFoto);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnRegistros = findViewById(R.id.btnRegistros);

        btnFoto.setOnClickListener(view -> showImageOptions());

        datePickerContainer.setOnClickListener(view -> mostrarDatePickerDialog());

        if (id == null || id.isEmpty()) {
            btnGuardar.setOnClickListener(view -> {
                String nombreContacto = nombre.getText().toString().trim();
                String apellidoContacto = apellido.getText().toString().trim();
                String correoContacto = correo.getText().toString().trim();
                String fechaContacto = fecha.getText().toString().trim();

                if (nombreContacto.isEmpty() || apellidoContacto.isEmpty() || correoContacto.isEmpty() || fechaContacto.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Campos Vacíos!", Toast.LENGTH_LONG).show();
                } else {
                    postContacto(nombreContacto, apellidoContacto, correoContacto, fechaContacto);
                    clear();
                }
            });
        } else {
            idd = id;
            btnGuardar.setText("Actualizar");
            getContacto(id);

            btnGuardar.setOnClickListener(view -> {
                String nombreContacto = nombre.getText().toString().trim();
                String apellidoContacto = apellido.getText().toString().trim();
                String correoContacto = correo.getText().toString().trim();
                String fechaContacto = fecha.getText().toString().trim();

                if (nombreContacto.isEmpty() || apellidoContacto.isEmpty() || correoContacto.isEmpty() || fechaContacto.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Campos Vacíos!", Toast.LENGTH_LONG).show();
                } else {
                    updateContacto(nombreContacto, apellidoContacto, correoContacto, fechaContacto, id);
                    clear();
                }
            });
        }

        btnRegistros.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ListaPersonas.class);
            startActivity(intent);
        });
    }

    private void mostrarDatePickerDialog() {
        final Calendar calendario = Calendar.getInstance();
        int año = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    String fechaSeleccionada = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year);
                    fecha.setText(fechaSeleccionada);
                }, año, mes, dia);
        datePickerDialog.show();
    }

    private void showImageOptions() {
        CharSequence[] opciones = {"Tomar Foto", "Elegir una de Galería"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Seleccionar imagen");
        builder.setItems(opciones, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else {
                uploadPhotoFromGallery();
            }
        });
        builder.show();
    }

    private void openCamera() {
        File file = new File(getExternalFilesDir(null), "photo_temp.jpg");
        photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, COD_TAKE_PHOTO);
    }

    private void uploadPhotoFromGallery() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, COD_SEL_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == COD_SEL_IMAGE && data != null) {
                image_url = data.getData();
                try {
                    InputStream imageStream = getContentResolver().openInputStream(image_url);
                    bitmapSelected = BitmapFactory.decodeStream(imageStream);
                    imageView.setImageBitmap(bitmapSelected);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == COD_TAKE_PHOTO && photoUri != null) {
                try {
                    bitmapSelected = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    imageView.setImageBitmap(bitmapSelected);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(getApplicationContext(), "No se seleccionó imagen", Toast.LENGTH_SHORT).show();
            return "";
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        byte[] imageBytes = stream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void postContacto(String nombreContacto, String apellidoContacto, String correoContacto, String fechaContacto) {
        String id = mDatabaseReference.push().getKey();
        String base64Image = convertBitmapToBase64(bitmapSelected);

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("nombre", nombreContacto);
        map.put("apellido", apellidoContacto);
        map.put("correo", correoContacto);
        map.put("fechaNacimiento", fechaContacto);
        map.put("photoBase64", base64Image);

        mDatabaseReference.child(id).setValue(map)
                .addOnSuccessListener(aVoid -> Toast.makeText(getApplicationContext(), "Registro Exitoso!", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Error al ingresar!", Toast.LENGTH_LONG).show());
    }

    private void updateContacto(String nombreContacto, String apellidoContacto, String correoContacto, String fechaContacto, String id) {
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", nombreContacto);
        map.put("apellido", apellidoContacto);
        map.put("correo", correoContacto);
        map.put("fechaNacimiento", fechaContacto);

        if (bitmapSelected != null) {
            map.put("photoBase64", convertBitmapToBase64(bitmapSelected));
        }

        mDatabaseReference.child(id).updateChildren(map)
                .addOnSuccessListener(aVoid -> Toast.makeText(getApplicationContext(), "Registro actualizado!", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Error al actualizar!", Toast.LENGTH_LONG).show());
    }

    private void getContacto(String id) {
        mDatabaseReference.child(id).get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                String nombrePersona = dataSnapshot.child("nombre").getValue(String.class);
                String apellidoPersona = dataSnapshot.child("apellido").getValue(String.class);
                String correoPersona = dataSnapshot.child("correo").getValue(String.class);
                String fechaNacPersona = dataSnapshot.child("fechaNacimiento").getValue(String.class);
                String photoBase64 = dataSnapshot.child("photoBase64").getValue(String.class);

                nombre.setText(nombrePersona);
                apellido.setText(apellidoPersona);
                correo.setText(correoPersona);
                fecha.setText(fechaNacPersona);

                if (photoBase64 != null && !photoBase64.isEmpty()) {
                    Log.d("MainActivity", "Base64 String: " + photoBase64);
                    try {
                        byte[] imageBytes = Base64.decode(photoBase64, Base64.DEFAULT);
                        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        imageView.setImageBitmap(decodedImage);
                    } catch (Exception e) {
                        Log.e("MainActivity", "Error decoding image", e);
                        imageView.setImageResource(R.drawable.ic_placeholder);
                    }
                } else {
                    imageView.setImageResource(R.drawable.ic_placeholder);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), "Error al obtener los datos!", Toast.LENGTH_LONG).show();
        });
    }

    private void clear() {
        nombre.setText("");
        apellido.setText("");
        correo.setText("");
        fecha.setText("");
        imageView.setImageResource(R.drawable.ic_placeholder);
        bitmapSelected = null;
    }
}
