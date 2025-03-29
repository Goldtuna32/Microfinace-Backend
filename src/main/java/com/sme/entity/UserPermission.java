//package com.sme.entity;
//
//import jakarta.persistence.*;
//import lombok.Data;
//
//@Entity
//@Data
//public class UserPermission {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    @ManyToOne
//    @JoinColumn(name = "permission_id")
//    private Permission permission;
//
//}