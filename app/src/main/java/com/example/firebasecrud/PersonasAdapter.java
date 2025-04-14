package com.example.firebasecrud;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.content.Intent;

import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class PersonasAdapter extends BaseAdapter {
    private Context context;
    private List<Personas> personas;

    public PersonasAdapter(Context context, List<Personas> personas) {
        this.context = context;
        this.personas = personas;
    }

    @Override
    public int getCount() {
        return personas.size();
    }

    @Override
    public Object getItem(int position) {
        return personas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        }

        Personas personas = this.personas.get(position);

        TextView nombreTextView = convertView.findViewById(R.id.contactNombre);
        TextView apellidoTextView = convertView.findViewById(R.id.contactApellido);
        TextView correoTextView = convertView.findViewById(R.id.contactCorreo);
        ImageView imageView = convertView.findViewById(R.id.contactImage);

        Button btnEditar = convertView.findViewById(R.id.btnEditar);
        Button btnEliminar = convertView.findViewById(R.id.btnEliminar);

        nombreTextView.setText(personas.getNombre());
        apellidoTextView.setText(personas.getApellido());
        correoTextView.setText(personas.getCorreo());

        String photoBase64 = personas.getPhotoBase64();
        if (photoBase64 != null && !photoBase64.isEmpty()) {
            byte[] imageBytes = Base64.decode(photoBase64, Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imageView.setImageBitmap(decodedImage);
        } else {
            imageView.setImageResource(R.drawable.ic_placeholder);
        }


        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("id_persona", personas.getId());
            context.startActivity(intent);
        });


        btnEliminar.setOnClickListener(v -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("persona").child(personas.getId());
            ref.removeValue().addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Persona Eliminada Correctamente", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Error Al Eliminar una Persona", Toast.LENGTH_SHORT).show();
            });
        });

        return convertView;
    }
}