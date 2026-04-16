package com.example.FoodProject.Repository;

import com.example.FoodProject.Model.NGO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NGORepository extends JpaRepository<NGO, Long> {
    NGO findByEmailAndPassword(String email, String password);

    @Query("""
            SELECT n FROM NGO n
            WHERE LOWER(n.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR n.mob LIKE CONCAT('%', :keyword, '%')
               OR LOWER(n.city) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(n.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    List<NGO> searchNgos(@Param("keyword") String keyword);
}