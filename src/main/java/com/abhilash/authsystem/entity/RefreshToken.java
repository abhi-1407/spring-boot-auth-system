package com.abhilash.authsystem.entity;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private Long expiryDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}