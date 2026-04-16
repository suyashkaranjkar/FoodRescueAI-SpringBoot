package com.example.FoodProject.Repository;

import com.example.FoodProject.Model.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RegRepository extends JpaRepository<Form,Long> {
        Form findByPermidAndMob(String permid, String mob);
        Form findByPermid(String permid);

        @Query("""
            SELECT f FROM Form f
            WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR f.mob LIKE CONCAT('%', :keyword, '%')
               OR LOWER(f.city) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
        List<Form> searchHotels(@Param("keyword") String keyword);
}
