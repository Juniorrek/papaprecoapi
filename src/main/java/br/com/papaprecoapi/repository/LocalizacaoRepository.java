package br.com.papaprecoapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.papaprecoapi.model.Localizacao;

@Repository
public interface LocalizacaoRepository extends JpaRepository<Localizacao,Integer> {
    Localizacao findByLatitudeAndLongitude(Double latitude, Double longitude);
}
