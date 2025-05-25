/**
 * Archivo principal de JavaScript para la aplicación EssenceDeToi
 * Incluye manejo de accesibilidad, notificaciones y funcionalidades generales
 */

// Función para mostrar notificaciones
function showNotification(message, type = 'success') {
    // Verificar si Bootstrap está disponible para mostrar notificaciones
    if (typeof bootstrap === 'undefined') {
        console.log(`[${type.toUpperCase()}] ${message}`);
        return;
    }
    
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
        if (bootstrap.Alert) {
            const bsAlert = new bootstrap.Alert(alertDiv);
            bsAlert.close();
        } else {
            alertDiv.remove();
        }
    }, 5000);
}

// Función para inicializar cuando el DOM esté listo
function initializeApp() {
    console.log('Inicializando aplicación...');
    
    // Inicializar funcionalidades que no dependen de jQuery
    initializeAccessibility();
    
    // Si jQuery está disponible, inicializar funcionalidades que lo requieran
    if (typeof jQuery !== 'undefined') {
        initializeJQueryFeatures();
    } else {
        console.warn('jQuery no está cargado - Algunas funcionalidades podrían no estar disponibles');
    }
}

// Inicializar características que no dependen de jQuery
function initializeAccessibility() {
    console.log('Inicializando accesibilidad...');
    
    // Inicializar el panel de accesibilidad
    const ttsToggle = document.getElementById('accessibility-toggle');
    const ttsPanel = document.getElementById('accessibility-panel');
    
    if (ttsToggle && ttsPanel) {
        // Asegurarse de que el botón sea visible
        ttsToggle.style.display = 'flex';
        ttsToggle.style.opacity = '1';
        
        // Mostrar el botón de accesibilidad
        ttsToggle.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            ttsPanel.classList.toggle('show');
            ttsToggle.setAttribute('aria-expanded', 
                ttsPanel.classList.contains('show').toString());
        });
        
        // Cerrar el panel al hacer clic en el botón de cerrar
        const closeBtn = document.getElementById('accessibility-close');
        if (closeBtn) {
            closeBtn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                ttsPanel.classList.remove('show');
                ttsToggle.setAttribute('aria-expanded', 'false');
            });
        }
        
        // Cerrar el panel al hacer clic fuera de él
        document.addEventListener('click', function(e) {
            if (ttsPanel && ttsToggle && 
                !ttsPanel.contains(e.target) && 
                !ttsToggle.contains(e.target)) {
                ttsPanel.classList.remove('show');
                ttsToggle.setAttribute('aria-expanded', 'false');
            }
        });
    }
    
    // Inicializar controles de TTS si existen
    if (typeof window.tts !== 'undefined') {
        const playPauseBtn = document.getElementById('tts-play-pause');
        const stopBtn = document.getElementById('tts-stop');
        
        if (playPauseBtn) {
            playPauseBtn.addEventListener('click', function() {
                if (!window.tts.currentText) {
                    const mainContent = document.querySelector('main, [role="main"], .container, .content') || document.body;
                    const textToRead = mainContent.innerText || mainContent.textContent;
                    window.tts.speak(textToRead);
                    
                    // Cambiar el ícono a pausa
                    const icon = playPauseBtn.querySelector('i');
                    if (icon) {
                        icon.className = 'fas fa-pause';
                    }
                } else {
                    window.tts.toggle();
                    
                    // Cambiar el ícono según el estado
                    const icon = playPauseBtn.querySelector('i');
                    if (icon) {
                        icon.className = window.tts.isSpeaking ? 'fas fa-pause' : 'fas fa-play';
                    }
                }
            });
        }
        
        if (stopBtn) {
            stopBtn.addEventListener('click', function() {
                window.tts.stop();
                
                // Restaurar el ícono de reproducción
                const icon = playPauseBtn ? playPauseBtn.querySelector('i') : null;
                if (icon) {
                    icon.className = 'fas fa-play';
                }
            });
        }
    }
}

// Inicializar características que requieren jQuery
function initializeJQueryFeatures() {
    if (typeof jQuery === 'undefined') {
        console.warn('jQuery no está disponible');
        return;
    }
    
    console.log('Inicializando características de jQuery...');
    
    // Manejar el envío de formularios en modales
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
}

// Manejo de errores global
window.onerror = function(message, source, lineno, colno, error) {
    // Ignorar errores específicos que no requieren notificación al usuario
    const ignoredErrors = [
        'ResizeObserver loop',
        'InvalidStateError',
        'NotSupportedError',
        'TypeError: Failed to set the \'value\' property on \'HTMLInputElement\': This input element does not support setting the value to empty string'
    ];

    const errorMessage = String(message);
    
    // Verificar si el error debe ser ignorado
    const shouldIgnore = ignoredErrors.some(ignoredError => errorMessage.includes(ignoredError));
    
    if (shouldIgnore) {
        console.warn('Error ignorado:', {message, source, lineno, colno, error});
        return true;
    }
    
    console.error('Error global:', {message, source, lineno, colno, error});
    
    // Mostrar notificación de error al usuario solo si no es un error de validación de fecha
    if (typeof bootstrap !== 'undefined' && !errorMessage.includes('appointmentDateTime')) {
        try {
            const existingAlerts = document.querySelectorAll('.alert.alert-danger');
            if (existingAlerts.length === 0) { // Evitar múltiples alertas
                const alertDiv = document.createElement('div');
                alertDiv.className = 'alert alert-danger alert-dismissible fade show position-fixed bottom-0 end-0 m-3';
                alertDiv.style.zIndex = '9999';
                alertDiv.role = 'alert';
                alertDiv.innerHTML = `
                    <strong>Error:</strong> Ha ocurrido un problema. Por favor, verifica los datos e inténtalo de nuevo.
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                `;
                document.body.appendChild(alertDiv);
                
                // Auto-ocultar después de 5 segundos
                setTimeout(() => {
                    if (bootstrap.Alert) {
                        const bsAlert = new bootstrap.Alert(alertDiv);
                        bsAlert.close();
                    } else {
                        alertDiv.remove();
                    }
                }, 5000);
            }
        } catch (e) {
            console.error('Error al mostrar notificación de error:', e);
        }
    }
    
    // Devolver true para evitar que el error se muestre en la consola del navegador
    return true;
};

// Inicializar la aplicación cuando el DOM esté listo
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeApp);
} else {
    // El DOM ya está listo
    setTimeout(initializeApp, 0);
}

// Hacer que la función showNotification esté disponible globalmente
window.showNotification = showNotification;
