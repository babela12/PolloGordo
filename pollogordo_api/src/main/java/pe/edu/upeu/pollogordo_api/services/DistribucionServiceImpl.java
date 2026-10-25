
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
        return distribucionRepository.findAll()
                .stream()
                .map(this::converTODTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DistribucionDTO> buscarPorId(Long id) {
        return distribucionRepository.findById(id)
                .map(this::converTODTO);
    }

    @Override
    @Transactional
    public DistribucionDTO crear(DistribucionDTO dto) {

        Pedido pedido = pedidoRepository.findById(dto.idPedido())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido ID: " + dto.idPedido() + " no existe"
                ));

        Insumo insumo = insumoRepository.findById(dto.idInsumo())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Insumo ID: " + dto.idInsumo() + " no existe"
                ));

        validarCantidad(dto.cantidad());
        descontarStock(insumo, dto.cantidad());

        Distribucion distribucion = new Distribucion();
        distribucion.setPedido(pedido);
        distribucion.setInsumo(insumo);
        distribucion.setCantidad(dto.cantidad());
        distribucion.setEstado(
                dto.estado() == null ? "GENERADO" : dto.estado()
        );

        return converTODTO(
                distribucionRepository.save(distribucion)
        );
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

            Producto producto = productoRepository.findById(
                    item.idProducto()
            ).orElseThrow(() -> new ResourceNotFoundException(
                    "Producto ID: " + item.idProducto() + " no existe"
            ));

            // El pollo crudo no requiere preparación.
            if ("POLLO CRUDO".equalsIgnoreCase(
                    producto.getNombre())) {
                continue;
            }

            // Solo los productos preparados generan insumos.
            if (!"POLLO".equalsIgnoreCase(producto.getTipo())
                    && !"CHULETA".equalsIgnoreCase(
                    producto.getTipo())) {
                continue;
            }

            String unidad = item.unidad()
                    .trim()
                    .toUpperCase();

            String acompanamientoActual =
                    validarAcompanamiento(
                            item.acompanamiento()
                    );

            // Solo PAPA o YUCA por pedido.
            if (acompanamiento == null) {
                acompanamiento = acompanamientoActual;
            } else if (!acompanamiento.equals(
                    acompanamientoActual)) {

                throw new IllegalArgumentException(
                        "Un pedido solo puede tener PAPA o YUCA"
                );
            }

            double cantidad = item.cantidad();
            validarCantidad(cantidad);

            double pollosEquivalentes =
                    convertirAPollos(cantidad, unidad);

            double porciones =
                    convertirAPorciones(cantidad, unidad);

            pollos += pollosEquivalentes;

            // Papa: 0.25 kg por cuarto u octavo.
            papa += porciones * 0.25;

            // Yuca: 0.005 sacos por porción.
            yuca += porciones * 0.005;

            // Ají y platos: uno por porción.
            aji += porciones;
            platos += porciones;

            // Arroz opcional.
            String arrozActual = validarTipoArroz(
                    item.tipoArroz()
            );

            if (arrozActual != null) {

                if (tipoArrozPedido == null) {
                    tipoArrozPedido = arrozActual;
                } else if (!tipoArrozPedido.equals(arrozActual)) {
                    throw new IllegalArgumentException(
                            "Un pedido no puede mezclar arroz árabe "
                                    + "y arroz a la jardinera"
                    );
                }

                if ("ARABE".equals(arrozActual)) {
                    arrozArabe += porciones;
                } else {
                    arrozJardinera += porciones;
                }
            }

            // Ensalada opcional.
            String ensaladaActual = validarTipoEnsalada(
                    item.tipoEnsalada()
            );

            if (ensaladaActual != null) {

                if (tipoEnsaladaPedido == null) {
                    tipoEnsaladaPedido = ensaladaActual;
                } else if (!tipoEnsaladaPedido.equals(
                        ensaladaActual)) {

                    throw new IllegalArgumentException(
                            "Un pedido no puede mezclar "
                                    + "ensalada blanca y ensalada fresca"
                    );
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

        Map<String, Double> distribuciones =
                new LinkedHashMap<>();

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
            distribuciones.put("Arroz arabe", arrozArabe);
        }

        if (arrozJardinera > 0) {
            distribuciones.put(
                    "Arroz a la jardinera",
                    arrozJardinera
            );
        }

        if (ensaladaBlanca > 0) {
            distribuciones.put(
                    "Ensalada blanca",
                    ensaladaBlanca
            );
        }

        if (ensaladaFresca > 0) {
            distribuciones.put(
                    "Ensalada fresca",
                    ensaladaFresca
            );
        }

        if (platos > 0) {
            distribuciones.put("Platos", platos);
        }

        // Carbón: 30 kg por cada 100 pollos.
        distribuciones.put("Carbon", pollos * 0.30);

        // Condimento: 1 litro por cada 10 pollos.
        distribuciones.put("Condimento", pollos * 0.10);

        // Guardar la distribución automática.
        for (Map.Entry<String, Double> entrada :
                distribuciones.entrySet()) {

            Insumo insumo = insumoRepository
                    .findByNombreIgnoreCase(entrada.getKey())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "El insumo "
                                            + entrada.getKey()
                                            + " no está registrado"
                            )
                    );

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

        return distribucionRepository.findById(id)
                .map(distribucion -> {

                    Pedido pedido = pedidoRepository.findById(
                            dto.idPedido()
                    ).orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Pedido ID: "
                                            + dto.idPedido()
                                            + " no existe"
                            )
                    );

                    Insumo nuevoInsumo =
                            insumoRepository.findById(
                                    dto.idInsumo()
                            ).orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Insumo ID: "
                                                    + dto.idInsumo()
                                                    + " no existe"
                                    )
                            );

                    validarCantidad(dto.cantidad());

                    Insumo anteriorInsumo =
                            distribucion.getInsumo();

                    double cantidadAnterior =
                            distribucion.getCantidad();

                    restaurarStock(
                            anteriorInsumo,
                            cantidadAnterior
                    );

                    descontarStock(
                            nuevoInsumo,
                            dto.cantidad()
                    );

                    distribucion.setPedido(pedido);
                    distribucion.setInsumo(nuevoInsumo);
                    distribucion.setCantidad(dto.cantidad());
                    distribucion.setEstado(dto.estado());

                    return converTODTO(
                            distribucionRepository.save(
                                    distribucion
                            )
                    );
                });
    }

    @Override
    @Transactional
    public boolean eliminar(Long id) {

        Optional<Distribucion> encontrada =
                distribucionRepository.findById(id);

        if (encontrada.isEmpty()) {
            return false;
        }

        Distribucion distribucion = encontrada.get();

        restaurarStock(
                distribucion.getInsumo(),
                distribucion.getCantidad()
        );

        distribucionRepository.delete(distribucion);

        return true;
    }

    private boolean controlaStock(Insumo insumo) {
        return "S".equalsIgnoreCase(
                insumo.getControlaStock()
        );
    }

    private void descontarStock(
            Insumo insumo,
            double cantidad) {

        if (!controlaStock(insumo)) {
            return;
        }

        Double stock = insumo.getCantidadStock();

        if (stock == null || stock < cantidad) {
            throw new InsufficientStockException(
                    "Stock insuficiente para el insumo: "
                            + insumo.getNombre()
            );
        }

        insumo.setCantidadStock(stock - cantidad);
        insumoRepository.save(insumo);
    }

    private void restaurarStock(
            Insumo insumo,
            double cantidad) {

        if (!controlaStock(insumo)) {
            return;
        }

        Double stock = insumo.getCantidadStock();

        if (stock == null) {
            throw new IllegalStateException(
                    "El insumo " + insumo.getNombre()
                            + " no tiene stock registrado"
            );
        }

        insumo.setCantidadStock(stock + cantidad);
        insumoRepository.save(insumo);
    }

    private void validarCantidad(Double cantidad) {

        if (cantidad == null
                || !Double.isFinite(cantidad)
                || cantidad <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor a cero"
            );
        }
    }

    private String validarAcompanamiento(
            String acompanamiento) {

        if (acompanamiento == null
                || acompanamiento.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El acompañamiento es obligatorio: PAPA o YUCA"
            );
        }

        String valor = acompanamiento
                .trim()
                .toUpperCase();

        if (!valor.equals("PAPA")
                && !valor.equals("YUCA")) {

            throw new IllegalArgumentException(
                    "El acompañamiento debe ser PAPA o YUCA"
            );
        }

        return valor;
    }

    private String validarTipoArroz(String tipoArroz) {

        if (tipoArroz == null
                || tipoArroz.trim().isEmpty()) {
            return null;
        }

        String valor = tipoArroz.trim().toUpperCase();

        if (!valor.equals("ARABE")
                && !valor.equals("JARDINERA")) {

            throw new IllegalArgumentException(
                    "El arroz debe ser ARABE o JARDINERA"
            );
        }

        return valor;
    }

    private String validarTipoEnsalada(
            String tipoEnsalada) {

        if (tipoEnsalada == null
                || tipoEnsalada.trim().isEmpty()) {
            return null;
        }

        String valor = tipoEnsalada.trim().toUpperCase();

        if (!valor.equals("BLANCA")
                && !valor.equals("FRESCA")) {

            throw new IllegalArgumentException(
                    "La ensalada debe ser BLANCA o FRESCA"
            );
        }

        return valor;
    }

    private double convertirAPollos(
            double cantidad,
            String unidad) {

        switch (unidad) {

            case "POLLO":
                return cantidad;

            case "CUARTO":
                return cantidad / 4.0;

            case "OCTAVO":
                return cantidad / 8.0;

            default:
                throw new IllegalArgumentException(
                        "Unidad no válida: " + unidad
                );
        }
    }

    private double convertirAPorciones(
            double cantidad,
            String unidad) {

        switch (unidad) {

            case "POLLO":
                // Por defecto, un pollo representa 4 cuartos.
                return cantidad * 4.0;

            case "CUARTO":
            case "OCTAVO":
                return cantidad;

            default:
                throw new IllegalArgumentException(
                        "Unidad no válida: " + unidad
                );
        }
    }

    private DistribucionDTO converTODTO(
            Distribucion d) {

        return new DistribucionDTO(
                d.getIdDistribucion(),
                d.getPedido().getIdPedido(),
                d.getInsumo().getIdInsumo(),
                d.getCantidad(),
                d.getEstado()
        );
    }
}
