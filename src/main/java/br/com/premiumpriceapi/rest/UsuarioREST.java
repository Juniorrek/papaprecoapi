package br.com.premiumpriceapi.rest;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import br.com.premiumpriceapi.repository.UsuarioRepository;

@CrossOrigin
@RestController
public class UsuarioREST {

    @Autowired 
    private UsuarioRepository repo;

    @Autowired
    private ModelMapper mapper;
    
}
