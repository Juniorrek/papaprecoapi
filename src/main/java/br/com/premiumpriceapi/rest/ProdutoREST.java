package br.com.premiumpriceapi.rest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
    
    @GetMapping(value = "/produtos")// , produces = "application/json;charset=UTF-8")
    public List<ProdutoDTO> buscarTodos(){

        List<Produto> lista = repo.findAll();     

        return lista.stream().map(e -> mapper.map(e, ProdutoDTO.class)).collect(Collectors.toList());
    }
    
    @GetMapping(value = "/produtos/{id}" , produces = "application/json;charset=UTF-8")
    public ProdutoDTO buscarPorId(@PathVariable("id") Integer id){

        Produto produto = repo.findById(id).get();     

        return mapper.map(produto, ProdutoDTO.class);
    }
    
    @GetMapping(value = "/produtos/nome/{nome}" , produces = "application/json;charset=UTF-8")
    public List<ProdutoDTO> buscarPorNome(@PathVariable("nome") String nome){

        List<Produto> lista = repo.findByNomeContainingIgnoringCase(nome);     

        return lista.stream().map(e -> mapper.map(e, ProdutoDTO.class)).collect(Collectors.toList());
    }
    
    @GetMapping(value = "/produtos/filtrar" , produces = "application/json;charset=UTF-8")
    public List<ProdutoDTO> filtrar(@RequestParam("nome") String nome ,
                                    @RequestParam("latitude") Double latitude,
                                    @RequestParam("longitude") Double longitude ,
                                    @RequestParam("distancia") Double distancia,
                                    @RequestParam("precoMin") Double precoMin,
                                    @RequestParam("precoMax") Double precoMax){

        List<Produto> lista = repo.findByNomeContainingIgnoreCaseAndPrecoBetween(nome, precoMin, precoMax); 
        
        lista = lista.stream()
                 .filter(p -> calculateDistance(p.getLatitude(), p.getLongitude(), latitude, longitude) <= distancia)
                 .collect(Collectors.toList());

        return lista.stream().map(e -> mapper.map(e, ProdutoDTO.class)).collect(Collectors.toList());
    }

    double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS = 6371;

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double lon1Rad = Math.toRadians(lon1);
        double lon2Rad = Math.toRadians(lon2);
    
        double x = (lon2Rad - lon1Rad) * Math.cos((lat1Rad + lat2Rad) / 2);
        double y = (lat2Rad - lat1Rad);
        double distance = Math.sqrt(x * x + y * y) * EARTH_RADIUS;
    
        return distance;
    }
    
    @GetMapping(value = "/produtos/ranking" , produces = "application/json;charset=UTF-8")
    public List<ProdutoDTO> buscarProdutosPorPalavraEPrecoRanking(@RequestParam("palavra") String palavra ,
                                    @RequestParam("latitude") Double latitude,
                                    @RequestParam("longitude") Double longitude,
                                    @RequestParam("distancia") Double distancia,
                                    @RequestParam("precoMin") Double precoMin,
                                    @RequestParam("precoMax") Double precoMax){

        //FILTRA PRECO E MATCH PALAVRA-NOME LEVENSHTEIN / AGRUPA PRODUTOS COM MESMO NOME LAT E LON E MOSTRA O MAIOR NO RANKING
        List<Produto> lista = repo.buscarProdutosPorPalavraEPrecoRanking(palavra, precoMin, precoMax); 
        
        //FILTRA DISTANCIA
        lista = lista.stream()
                 .filter(p -> calculateDistance(p.getLatitude(), p.getLongitude(), latitude, longitude) <= distancia)
                 .collect(Collectors.toList());

        return lista.stream().map(e -> mapper.map(e, ProdutoDTO.class)).collect(Collectors.toList());
    }
    
    @GetMapping(value = "/produtos/historico" , produces = "application/json;charset=UTF-8")
    public List<ProdutoDTO> buscarHistoricoProdutoRanking(@RequestParam("nome") String nome ,
                                    @RequestParam("latitude") Double latitude,
                                    @RequestParam("longitude") Double longitude){

        List<Produto> lista = repo.buscarHistoricoProdutoRanking(nome, latitude, longitude);

        return lista.stream().map(e -> mapper.map(e, ProdutoDTO.class)).collect(Collectors.toList());
    }
    
    @GetMapping(value = "/produtos/atual" , produces = "application/json;charset=UTF-8")
    public ProdutoDTO buscarProdutoAtualRanking(@RequestParam("nome") String nome ,
                                    @RequestParam("latitude") Double latitude,
                                    @RequestParam("longitude") Double longitude){

        List<Produto> lista = repo.buscarHistoricoProdutoRanking(nome, latitude, longitude);

        return mapper.map(lista.get(0), ProdutoDTO.class);
    }

    @PostMapping(value = "/produtos", produces = "application/json;charset=UTF-8")
    public ProdutoDTO inserir(@RequestBody ProdutoDTO produto) {
        // salva a Entidade convertida do DTO
        Produto p = mapper.map(produto, Produto.class);
        p = repo.save(p);     

        // busca o usuário inserido
        Optional<Produto> produt = repo.findById(p.getId());
        // retorna o DTO equivalente à entidade
        return mapper.map(produt, ProdutoDTO.class);
    }
}
