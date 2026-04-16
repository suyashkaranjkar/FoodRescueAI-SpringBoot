package com.example.FoodProject.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import java.time.LocalDate;

@Entity
public class Food {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String permid;
    
    private String foodName;
    
    private String foodType;
    
    private Integer quantity;
    
    private String quantityUnit;
    
    private LocalDate cookedWhen;
    
    private LocalDate freshUntil;
    
    private LocalDate expiryDate;
    
    private String photoFilename;

    
    @jakarta.persistence.Convert(converter = FoodStatusConverter.class)
    private FoodStatus status; // changed to enum

    private String acceptedByNgo; // NGO name for reference
    private String acceptedNgoName;
    private String acceptedNgoMobile;
    private String acceptedNgoEmail;
    private String acceptedNgoAddress;
    private String acceptedNgoCity;
    private String hotelRejectionReason;

    @Transient
    private String hotelName;

    @Transient
    private String hotelMobile;

    @Transient
    private String hotelAddress;

    @Transient
    private Integer hotelDistanceKm;

    private Long createdByHotelId;

    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    public Food() {
    }
    
    public Food(String permid, String foodName, String foodType, Integer quantity, String quantityUnit, LocalDate cookedWhen, LocalDate freshUntil, String photoFilename, FoodStatus status) {
        this.permid = permid;
        this.foodName = foodName;
        this.foodType = foodType;
        this.quantity = quantity;
        this.quantityUnit = quantityUnit;
        this.cookedWhen = cookedWhen;
        this.freshUntil = freshUntil;
        this.expiryDate = freshUntil;
        this.photoFilename = photoFilename;
        this.status = status;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPermid() {
        return permid;
    }
    
    public void setPermid(String permid) {
        this.permid = permid;
    }
    
    public String getFoodName() {
        return foodName;
    }
    
    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }
    
    public String getFoodType() {
        return foodType;
    }
    
    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getQuantityUnit() {
        return quantityUnit;
    }
    
    public void setQuantityUnit(String quantityUnit) {
        this.quantityUnit = quantityUnit;
    }
    
    public LocalDate getCookedWhen() {
        return cookedWhen;
    }
    
    public void setCookedWhen(LocalDate cookedWhen) {
        this.cookedWhen = cookedWhen;
    }
    
    public LocalDate getFreshUntil() {
        return freshUntil;
    }
    
    public void setFreshUntil(LocalDate freshUntil) {
        this.freshUntil = freshUntil;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getPhotoFilename() {
        return photoFilename;
    }

    public void setPhotoFilename(String photoFilename) {
        this.photoFilename = photoFilename;
    }

    public FoodStatus getStatus() {
        return status;
    }

    public void setStatus(FoodStatus status) {
        this.status = status;
    }

    public String getAcceptedByNgo() {
        return acceptedByNgo;
    }

    public void setAcceptedByNgo(String acceptedByNgo) {
        this.acceptedByNgo = acceptedByNgo;
    }

    public String getAcceptedNgoName() {
        return acceptedNgoName;
    }

    public void setAcceptedNgoName(String acceptedNgoName) {
        this.acceptedNgoName = acceptedNgoName;
    }

    public String getAcceptedNgoMobile() {
        return acceptedNgoMobile;
    }

    public void setAcceptedNgoMobile(String acceptedNgoMobile) {
        this.acceptedNgoMobile = acceptedNgoMobile;
    }

    public String getAcceptedNgoEmail() {
        return acceptedNgoEmail;
    }

    public void setAcceptedNgoEmail(String acceptedNgoEmail) {
        this.acceptedNgoEmail = acceptedNgoEmail;
    }

    public String getAcceptedNgoAddress() {
        return acceptedNgoAddress;
    }

    public void setAcceptedNgoAddress(String acceptedNgoAddress) {
        this.acceptedNgoAddress = acceptedNgoAddress;
    }

    public String getAcceptedNgoCity() {
        return acceptedNgoCity;
    }

    public void setAcceptedNgoCity(String acceptedNgoCity) {
        this.acceptedNgoCity = acceptedNgoCity;
    }

    public String getHotelRejectionReason() {
        return hotelRejectionReason;
    }

    public void setHotelRejectionReason(String hotelRejectionReason) {
        this.hotelRejectionReason = hotelRejectionReason;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getHotelMobile() {
        return hotelMobile;
    }

    public void setHotelMobile(String hotelMobile) {
        this.hotelMobile = hotelMobile;
    }

    public String getHotelAddress() {
        return hotelAddress;
    }

    public void setHotelAddress(String hotelAddress) {
        this.hotelAddress = hotelAddress;
    }

    public Integer getHotelDistanceKm() {
        return hotelDistanceKm;
    }

    public void setHotelDistanceKm(Integer hotelDistanceKm) {
        this.hotelDistanceKm = hotelDistanceKm;
    }

    public Long getCreatedByHotelId() {
        return createdByHotelId;
    }

    public void setCreatedByHotelId(Long createdByHotelId) {
        this.createdByHotelId = createdByHotelId;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
