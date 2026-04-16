package com.example.FoodProject.Repository;

import com.example.FoodProject.Model.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByPermid(String permid);
    List<Food> findByStatus(com.example.FoodProject.Model.FoodStatus status);
    List<Food> findByAcceptedNgoName(String acceptedNgoName);
    List<Food> findByAcceptedNgoNameIgnoreCase(String acceptedNgoName);
    List<Food> findByAcceptedNgoEmailIgnoreCase(String acceptedNgoEmail);
}
