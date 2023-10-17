package com.example.phototeca;

import com.example.phototeca.model.Cryptocurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CryptocurrencyRepository extends JpaRepository<Cryptocurrency, Long> {

    Cryptocurrency findBySymbol(String symbol);
}
