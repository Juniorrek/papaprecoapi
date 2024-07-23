package br.com.premiumpriceapi.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.premiumpriceapi.dto.ProdutoDTO;
import br.com.premiumpriceapi.model.Produto;
import br.com.premiumpriceapi.repository.ProdutoRepository;

@CrossOrigin
@RestController
public class ProdutoREST {

    @Autowired
    private ProdutoRepository repo;

    @Autowired
    private ModelMapper mapper;
    
    @GetMapping(value = "/produtos/")// , produces = "application/json;charset=UTF-8")
    public List<ProdutoDTO> buscarPorNome(){

        List<Produto> lista = repo.findAll();     

        return lista.stream().map(e -> mapper.map(e, ProdutoDTO.class)).collect(Collectors.toList());
    }
}
