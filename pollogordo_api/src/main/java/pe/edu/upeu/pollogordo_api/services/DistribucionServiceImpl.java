
package pe.edu.upeu.pollogordo_api.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.pollogordo_api.dto.DistribucionDTO;
import pe.edu.upeu.pollogordo_api.dto.ItemPedidoDTO;
import pe.edu.upeu.pollogordo_api.entity.Distribucion;
import pe.edu.upeu.pollogordo_api.entity.Insumo;
import pe.edu.upeu.pollogordo_api.entity.Pedido;
import pe.edu.upeu.pollogordo_api.entity.Producto;
import pe.edu.upeu.pollogordo_api.exception.InsufficientStockException;
import pe.edu.upeu.pollogordo_api.exception.ResourceNotFoundException;
import pe.edu.upeu.pollogordo_api.repository.DistribucionRepository;
import pe.edu.upeu.pollogordo_api.repository.InsumoRepository;
import pe.edu.upeu.pollogordo_api.repository.PedidoRepository;
import pe.edu.upeu.pollogordo_api.repository.ProductoRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DistribucionServiceImpl implements IDistribucionService {

    private final DistribucionRepository distribucionRepository;
    private final PedidoRepository pedidoRepository;
    private final InsumoRepository insumoRepository;
    private final ProductoRepository productoRepository;

    public DistribucionServiceImpl(
            DistribucionRepository distribucionRepository,
            PedidoRepository pedidoRepository,
            InsumoRepository insumoRepository,
            ProductoRepository productoRepository) {

        this.distribucionRepository = distribucionRepository;
        this.pedidoRepository = pedidoRepository;
        this.insumoRepository = insumoRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DistribucionDTO> listarTodo() {

        List<DistribucionDTO> lista = new ArrayList<>();

        for (Distribucion distribucion : distribucionRepository.findAll()) {
            lista.add(convertirDTO(distribucion));
        }

        return lista;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DistribucionDTO> buscarPorId(Long id) {

        Optional<Distribucion> resultado =
                distribucionRepository.findById(id);

        if (resultado.isPresent()) {
            return Optional.of(convertirDTO(resultado.get()));
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public DistribucionDTO crear(DistribucionDTO dto) {

        Pedido pedido = buscarPedido(dto.idPedido());
        Insumo insumo = buscarInsumo(dto.idInsumo());

        validarCantidad(dto.cantidad());
        descontarStock(insumo, dto.cantidad());

        Distribucion distribucion = new Distribucion();
        distribucion.setPedido(pedido);
        distribucion.setInsumo(insumo);
        distribucion.setCantidad(dto.cantidad());

        if (dto.estado() == null) {
            distribucion.setEstado("GENERADO");
        } else {
            distribucion.setEstado(dto.estado());
        }

        distribucionRepository.save(distribucion);

        return convertirDTO(distribucion);
    }

    @Override
    @Transactional
    public void generarDistribucion(
            Pedido pedido,
            List<ItemPedidoDTO> items) {

        double pollos = 0;
        double papa = 0;
        double yuca = 0;
        double aji = 0;
        double platos = 0;

        double arrozArabe = 0;
        double arrozJardinera = 0;
        double ensaladaBlanca = 0;
        double ensaladaFresca = 0;

        String acompanamiento = null;
        String tipoArrozPedido = null;
        String tipoEnsaladaPedido = null;

        for (ItemPedidoDTO item : items) {

            Producto producto = buscarProducto(item.idProducto());

            if ("POLLO CRUDO".equalsIgnoreCase(producto.getNombre())) {
                continue;
            }

            if (!"POLLO".equalsIgnoreCase(producto.getTipo())
                    && !"CHULETA".equalsIgnoreCase(producto.getTipo())) {
                continue;
            }

            String unidad = item.unidad().trim().toUpperCase();

            String acompanamientoActual =
                    validarAcompanamiento(item.acompanamiento());

            if (acompanamiento == null) {
                acompanamiento = acompanamientoActual;
            } else if (!acompanamiento.equals(acompanamientoActual)) {
                throw new IllegalArgumentException(
                        "Un pedido solo puede tener PAPA o YUCA");
            }

            double cantidad = item.cantidad();
            validarCantidad(cantidad);

            double pollosEquivalentes =
                    convertirAPollos(cantidad, unidad);

            double porciones =
                    convertirAPorciones(cantidad, unidad);

            pollos += pollosEquivalentes;
            papa += porciones * 0.25;
            yuca += porciones * 0.005;
            aji += porciones;
            platos += porciones;

            String arrozActual = validarTipoArroz(item.tipoArroz());

            if (arrozActual != null) {

                if (tipoArrozPedido == null) {
                    tipoArrozPedido = arrozActual;
                } else if (!tipoArrozPedido.equals(arrozActual)) {
                    throw new IllegalArgumentException(
                            "Un pedido no puede mezclar arroz árabe y arroz a la jardinera");
                }

                if ("ARABE".equals(arrozActual)) {
                    arrozArabe += porciones;
                } else {
                    arrozJardinera += porciones;
                }
            }

            String ensaladaActual =
                    validarTipoEnsalada(item.tipoEnsalada());

            if (ensaladaActual != null) {

                if (tipoEnsaladaPedido == null) {
                    tipoEnsaladaPedido = ensaladaActual;
                } else if (!tipoEnsaladaPedido.equals(ensaladaActual)) {
                    throw new IllegalArgumentException(
                            "Un pedido no puede mezclar ensalada blanca y ensalada fresca");
                }

                if ("BLANCA".equals(ensaladaActual)) {
                    ensaladaBlanca += porciones;
                } else {
                    ensaladaFresca += porciones;
                }
            }
        }

        if (pollos <= 0) {
            return;
        }

        Map<String, Double> distribuciones = new LinkedHashMap<>();

        if ("PAPA".equals(acompanamiento) && papa > 0) {
            distribuciones.put("Papa", papa);
        }

        if ("YUCA".equals(acompanamiento) && yuca > 0) {
            distribuciones.put("Yuca", yuca);
        }

        if (aji > 0) {
            distribuciones.put("Aji", aji);
        }

        if (arrozArabe > 0) {
            distribuciones.put("Arroz árabe", arrozArabe);
        }

        if (arrozJardinera > 0) {
            distribuciones.put("Arroz a la jardinera", arrozJardinera);
        }

        if (ensaladaBlanca > 0) {
            distribuciones.put("Ensalada blanca", ensaladaBlanca);
        }

        if (ensaladaFresca > 0) {
            distribuciones.put("Ensalada fresca", ensaladaFresca);
        }

        if (platos > 0) {
            distribuciones.put("Platos", platos);
        }

        distribuciones.put("Carbón", pollos * 0.30);
        distribuciones.put("Condimento", pollos * 0.10);

        for (Map.Entry<String, Double> entrada : distribuciones.entrySet()) {

            Optional<Insumo> resultado =
                    insumoRepository.findByNombreIgnoreCase(entrada.getKey());

            if (resultado.isEmpty()) {
                throw new ResourceNotFoundException(
                        "El insumo " + entrada.getKey() + " no está registrado");
            }

            Insumo insumo = resultado.get();
            double cantidadNecesaria = entrada.getValue();

            descontarStock(insumo, cantidadNecesaria);

            Distribucion distribucion = new Distribucion();
            distribucion.setPedido(pedido);
            distribucion.setInsumo(insumo);
            distribucion.setCantidad(cantidadNecesaria);
            distribucion.setEstado("GENERADO");

            distribucionRepository.save(distribucion);
        }
    }

    @Override
    @Transactional
    public Optional<DistribucionDTO> actualizar(
            Long id,
            DistribucionDTO dto) {

        Optional<Distribucion> resultado =
                distribucionRepository.findById(id);

        if (resultado.isEmpty()) {
            return Optional.empty();
        }

        Distribucion distribucion = resultado.get();

        Pedido pedido = buscarPedido(dto.idPedido());
        Insumo nuevoInsumo = buscarInsumo(dto.idInsumo());

        validarCantidad(dto.cantidad());

        Insumo anteriorInsumo = distribucion.getInsumo();
        double cantidadAnterior = distribucion.getCantidad();

        restaurarStock(anteriorInsumo, cantidadAnterior);
        descontarStock(nuevoInsumo, dto.cantidad());

        distribucion.setPedido(pedido);
        distribucion.setInsumo(nuevoInsumo);
        distribucion.setCantidad(dto.cantidad());
        distribucion.setEstado(dto.estado());

        distribucionRepository.save(distribucion);

        return Optional.of(convertirDTO(distribucion));
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {

        Optional<Distribucion> resultado =
                distribucionRepository.findById(id);

        if (resultado.isEmpty()) {
            return false;
        }

        Distribucion distribucion = resultado.get();

        restaurarStock(
                distribucion.getInsumo(),
                distribucion.getCantidad()
        );

        distribucionRepository.delete(distribucion);

        return true;
    }

    private Pedido buscarPedido(Long id) {

        Optional<Pedido> resultado = pedidoRepository.findById(id);

        if (resultado.isPresent()) {
            return resultado.get();
        }

        throw new ResourceNotFoundException(
                "Pedido ID: " + id + " no existe");
    }

    private Insumo buscarInsumo(Long id) {

        Optional<Insumo> resultado = insumoRepository.findById(id);

        if (resultado.isPresent()) {
            return resultado.get();
        }

        throw new ResourceNotFoundException(
                "Insumo ID: " + id + " no existe");
    }

    private Producto buscarProducto(Long id) {

        Optional<Producto> resultado = productoRepository.findById(id);

        if (resultado.isPresent()) {
            return resultado.get();
        }

        throw new ResourceNotFoundException(
                "Producto ID: " + id + " no existe");
    }

    private boolean controlaStock(Insumo insumo) {
        return "S".equalsIgnoreCase(insumo.getControlaStock());
    }

    private void descontarStock(Insumo insumo, double cantidad) {

        if (!controlaStock(insumo)) {
            return;
        }

        Double stock = insumo.getCantidadStock();

        if (stock == null || stock < cantidad) {
            throw new InsufficientStockException(
                    "Stock insuficiente para el insumo: " + insumo.getNombre());
        }

        insumo.setCantidadStock(stock - cantidad);
        insumoRepository.save(insumo);
    }

    private void restaurarStock(Insumo insumo, double cantidad) {

        if (!controlaStock(insumo)) {
            return;
        }

        Double stock = insumo.getCantidadStock();

        if (stock == null) {
            throw new IllegalStateException(
                    "El insumo " + insumo.getNombre()
                            + " no tiene stock registrado");
        }

        insumo.setCantidadStock(stock + cantidad);
        insumoRepository.save(insumo);
    }

    private void validarCantidad(Double cantidad) {

        if (cantidad == null
                || !Double.isFinite(cantidad)
                || cantidad <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor a cero");
        }
    }

    private String validarAcompanamiento(String acompanamiento) {

        if (acompanamiento == null
                || acompanamiento.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El acompañamiento es obligatorio: PAPA o YUCA");
        }

        String valor = acompanamiento.trim().toUpperCase();

        if (!valor.equals("PAPA") && !valor.equals("YUCA")) {
            throw new IllegalArgumentException(
                    "El acompañamiento debe ser PAPA o YUCA");
        }

        return valor;
    }

    private String validarTipoArroz(String tipoArroz) {

        if (tipoArroz == null || tipoArroz.trim().isEmpty()) {
            return null;
        }

        String valor = tipoArroz.trim().toUpperCase();

        if (!valor.equals("ARABE") && !valor.equals("JARDINERA")) {
            throw new IllegalArgumentException(
                    "El arroz debe ser ARABE o JARDINERA");
        }

        return valor;
    }

    private String validarTipoEnsalada(String tipoEnsalada) {

        if (tipoEnsalada == null || tipoEnsalada.trim().isEmpty()) {
            return null;
        }

        String valor = tipoEnsalada.trim().toUpperCase();

        if (!valor.equals("BLANCA") && !valor.equals("FRESCA")) {
            throw new IllegalArgumentException(
                    "La ensalada debe ser BLANCA o FRESCA");
        }

        return valor;
    }

    private double convertirAPollos(double cantidad, String unidad) {

        switch (unidad) {
            case "POLLO":
                return cantidad;

            case "CUARTO":
                return cantidad / 4.0;

            case "OCTAVO":
                return cantidad / 8.0;

            default:
                throw new IllegalArgumentException(
                        "Unidad no válida: " + unidad);
        }
    }

    private double convertirAPorciones(double cantidad, String unidad) {

        switch (unidad) {
            case "POLLO":
                return cantidad * 4.0;

            case "CUARTO":
            case "OCTAVO":
                return cantidad;

            default:
                throw new IllegalArgumentException(
                        "Unidad no válida: " + unidad);
        }
    }

    private DistribucionDTO convertirDTO(Distribucion distribucion) {

        return new DistribucionDTO(
                distribucion.getIdDistribucion(),
                distribucion.getPedido().getIdPedido(),
                distribucion.getInsumo().getIdInsumo(),
                distribucion.getCantidad(),
                distribucion.getEstado()
        );
    }
}
