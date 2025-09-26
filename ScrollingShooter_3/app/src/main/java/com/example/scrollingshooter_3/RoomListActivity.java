package com.example.scrollingshooter_3;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RoomListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RoomAdapter adapter;
    private List<GameRoom> roomList;
    private DatabaseReference roomsRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        recyclerView = findViewById(R.id.roomRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        roomList = new ArrayList<>();
        adapter = new RoomAdapter(roomList);
        recyclerView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        roomsRef = FirebaseDatabase.getInstance().getReference("rooms");

        loadRoomList();
    }

    private void loadRoomList() {
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomList.clear();

                for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                    GameRoom room = roomSnapshot.getValue(GameRoom.class);
                    if (room != null && !room.isGameStarted) {
                        roomList.add(room);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RoomListActivity.this, "방 목록 로드 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
        private List<GameRoom> rooms;

        RoomAdapter(List<GameRoom> rooms) {
            this.rooms = rooms;
        }

        @NonNull
        @Override
        public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_room, parent, false);
            return new RoomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
            GameRoom room = rooms.get(position);
            holder.roomNameText.setText("방 #" + (position + 1));

            // 방 호스트 이름 표시
            if (room.players != null && !room.players.isEmpty()) {
                for (MultiPlayer player : room.players.values()) {
                    if (player.isHost) {
                        holder.hostNameText.setText("방장: " + player.name);
                        break;
                    }
                }
            }

            holder.joinButton.setOnClickListener(v -> showJoinDialog(room.roomId));
        }

        @Override
        public int getItemCount() {
            return rooms.size();
        }

        class RoomViewHolder extends RecyclerView.ViewHolder {
            TextView roomNameText;
            TextView hostNameText;
            Button joinButton;

            RoomViewHolder(View itemView) {
                super(itemView);
                roomNameText = itemView.findViewById(R.id.roomNameText);
                hostNameText = itemView.findViewById(R.id.hostNameText);
                joinButton = itemView.findViewById(R.id.joinButton);
            }
        }
    }

    private void showJoinDialog(String roomId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("방 참여하기");

        final EditText input = new EditText(this);
        input.setHint("플레이어 이름");
        builder.setView(input);

        builder.setPositiveButton("참여하기", (dialog, which) -> {
            String playerName = input.getText().toString();
            if (!playerName.isEmpty()) {
                joinRoom(roomId, playerName);
            } else {
                Toast.makeText(this, "이름을 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void joinRoom(String roomId, String playerName) {
        if (auth.getCurrentUser() == null) return;

        String playerId = auth.getCurrentUser().getUid();

        // 새 플레이어 정보 생성
        MultiPlayer newPlayer = new MultiPlayer(playerId, playerName, false);

        // 방에 플레이어 추가
        roomsRef.child(roomId).child("players").child(playerId).setValue(newPlayer)
                .addOnSuccessListener(aVoid -> {
                    // 참여 성공, 게임 화면으로 이동
                    Intent intent = new Intent(RoomListActivity.this, MultiplayerGameActivity.class);
                    intent.putExtra("ROOM_ID", roomId);
                    intent.putExtra("PLAYER_ID", playerId);
                    intent.putExtra("IS_HOST", false);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "방 참여 실패", Toast.LENGTH_SHORT).show();
                });

        // 연결 종료 시 플레이어 데이터 삭제
        DatabaseReference playerRef = roomsRef.child(roomId).child("players").child(playerId);
        playerRef.onDisconnect().removeValue();
    }
}