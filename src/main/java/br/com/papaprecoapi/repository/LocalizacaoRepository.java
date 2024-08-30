package br.com.papaprecoapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.papaprecoapi.model.Localizacao;

public interface LocalizacaoRepository extends JpaRepository<Localizacao,Integer> {
    Localizacao findByLatitudeAndLongitude(Double latitude, Double longitude);
}
