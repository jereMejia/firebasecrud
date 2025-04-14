package com.example.firebasecrud;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListaPersonas extends AppCompatActivity {

    private ListView listView;
    private ArrayList<Personas> personasList;
    private DatabaseReference mDatabaseReference;
    private PersonasAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_registros);

        listView = findViewById(R.id.listView);
        personasList = new ArrayList<>();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("personas");

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                personasList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Personas personas = snapshot.getValue(Personas.class);
                    personasList.add(personas);
                }
                adapter = new PersonasAdapter(ListaPersonas.this, personasList);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ListaPersonas.this, "Error al obtener los datos", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
