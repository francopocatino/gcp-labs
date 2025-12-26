package com.example.lab09;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
  // JpaRepository provides: save, findById, findAll, deleteById, etc.
}
