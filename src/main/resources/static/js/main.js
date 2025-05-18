// Manejar el envío de formularios en modales
document.addEventListener('DOMContentLoaded', function() {
    // Verificar que jQuery esté disponible
    if (typeof jQuery === 'undefined') {
        console.error('jQuery no está cargado correctamente');
        return;
    }

    // Verificar que Bootstrap esté disponible
    if (typeof bootstrap === 'undefined') {
        console.error('Bootstrap no está cargado correctamente');
        return;
    }

    // Función para mostrar notificaciones
    function showNotification(message, type = 'success') {
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed top-0 end-0 m-3`;
        alertDiv.style.zIndex = '9999';
        alertDiv.role = 'alert';
        alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;
        document.body.appendChild(alertDiv);
        
        // Auto-ocultar después de 5 segundos
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alertDiv);
            bsAlert.close();
        }, 5000);
    }

    // Manejar envío de formularios en modales
    $(document).on('submit', '.modal form', function(e) {
        e.preventDefault();
        
        const form = $(this);
        const modal = form.closest('.modal');
        const submitButton = form.find('button[type="submit"]');
        const originalButtonText = submitButton.html();
        
        // Deshabilitar botón para evitar múltiples envíos
        submitButton.prop('disabled', true).html(
            '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Procesando...'
        );
        
        try {
            // Obtener el token CSRF
            const csrfToken = $('meta[name="_csrf"]').attr('content');
            const csrfHeader = $('meta[name="_csrf_header"]').attr('content');
            
            if (!csrfToken || !csrfHeader) {
                throw new Error('No se pudo verificar la seguridad de la solicitud. Por favor, recarga la página.');
            }
            
            // Enviar el formulario usando fetch
            fetch(form.attr('action'), {
                method: form.attr('method') || 'POST',
                headers: {
                    [csrfHeader]: csrfToken,
                    'Accept': 'application/json',
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams(new FormData(form[0]))
            })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        throw new Error(`Error HTTP: ${response.status} - ${text}`);
                    });
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    showNotification(data.message || 'Operación exitosa', 'success');
                    
                    // Cerrar el modal
                    if (modal.length) {
                        const bsModal = bootstrap.Modal.getInstance(modal[0]);
                        if (bsModal) bsModal.hide();
                    }
                    
                    // Redirigir si es necesario
                    if (data.redirectUrl) {
                        window.location.href = data.redirectUrl;
                    } else {
                        // Recargar la página si no hay redirección
                        window.location.reload();
                    }
                } else {
                    throw new Error(data.message || 'Error al procesar la solicitud');
                }
            })
            .catch(error => {
                console.error('Error en la solicitud:', error);
                showNotification(error.message || 'Error al procesar la solicitud', 'danger');
            })
            .finally(() => {
                // Restaurar el botón
                submitButton.prop('disabled', false).html(originalButtonText);
            });
                
        } catch (error) {
            console.error('Error inesperado:', error);
            showNotification(error.message || 'Ocurrió un error inesperado', 'danger');
            submitButton.prop('disabled', false).html(originalButtonText);
        }
    });
});

// Manejo de errores global
window.onerror = function(message, source, lineno, colno, error) {
    console.error('Error global:', {message, source, lineno, colno, error});
    
    // Mostrar notificación de error al usuario
    if (typeof bootstrap !== 'undefined') {
        const alertDiv = document.createElement('div');
        alertDiv.className = 'alert alert-danger alert-dismissible fade show position-fixed bottom-0 end-0 m-3';
        alertDiv.style.zIndex = '9999';
        alertDiv.role = 'alert';
        alertDiv.innerHTML = `
            <strong>Error:</strong> Ha ocurrido un error inesperado. Por favor, recarga la página.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;
        document.body.appendChild(alertDiv);
    }
    
    // Prevenir el manejador de errores por defecto
    return true;
};
