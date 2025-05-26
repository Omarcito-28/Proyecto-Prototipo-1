class TextToSpeech {
    constructor() {
        this.speechSynthesis = window.speechSynthesis;
        this.speechUtterance = null;
        this.isSpeaking = false;
        this.currentText = '';
        this.savedPosition = 0;
        
        // Configuración por defecto
        this.settings = {
            rate: 1.0,
            pitch: 1.0,
            volume: 1.0,
            voice: null,
            lang: 'es-ES'
        };
        
        this.init();
    }
    
    async init() {
        // Cargar configuraciones guardadas
        this.loadSettings();
        
        // Cargar voces disponibles
        await this.loadVoices();
        
        // Actualizar controles de la interfaz
        this.updateUIControls();
    }
    
    async loadVoices() {
        return new Promise((resolve) => {
            // Esperar a que las voces estén cargadas
            const checkVoices = () => {
                const voices = this.speechSynthesis.getVoices();
                if (voices.length > 0) {
                    this.voices = voices;
                    // Seleccionar voz predeterminada en español si está disponible
                    const spanishVoice = voices.find(v => v.lang.startsWith('es'));
                    if (spanishVoice) {
                        this.settings.voice = spanishVoice;
                    } else if (voices.length > 0) {
                        this.settings.voice = voices[0];
                    }
                    resolve();
                } else {
                    setTimeout(checkVoices, 100);
                }
            };
            checkVoices();
        });
    }
    
    loadSettings() {
        const savedSettings = localStorage.getItem('ttsSettings');
        if (savedSettings) {
            try {
                this.settings = { ...this.settings, ...JSON.parse(savedSettings) };
            } catch (e) {
                console.error('Error al cargar configuraciones TTS:', e);
            }
        }
    }
    
    saveSettings() {
        try {
            localStorage.setItem('ttsSettings', JSON.stringify(this.settings));
        } catch (e) {
            console.error('Error al guardar configuraciones TTS:', e);
        }
    }
    
    updateUIControls() {
        // Actualizar controles de la interfaz si existen
        const rateInput = document.getElementById('tts-rate');
        const pitchInput = document.getElementById('tts-pitch');
        const volumeInput = document.getElementById('tts-volume');
        const voiceSelect = document.getElementById('tts-voice');
        
        if (rateInput) rateInput.value = this.settings.rate;
        if (pitchInput) pitchInput.value = this.settings.pitch;
        if (volumeInput) volumeInput.value = this.settings.volume;
        
        // Llenar selector de voces
        if (voiceSelect && this.voices) {
            voiceSelect.innerHTML = '';
            this.voices.forEach((voice, index) => {
                const option = document.createElement('option');
                option.value = index;
                option.textContent = `${voice.name} (${voice.lang})`;
                if (this.settings.voice && voice.voiceURI === this.settings.voice.voiceURI) {
                    option.selected = true;
                }
                voiceSelect.appendChild(option);
            });
        }
    }
    
    speak(text, element = null) {
        try {
            // Verificar si el navegador soporta la síntesis de voz
            if (!('speechSynthesis' in window)) {
                throw new Error('Tu navegador no soporta la síntesis de voz');
            }

            // Verificar si hay texto para leer
            if (!text || typeof text !== 'string' || text.trim() === '') {
                throw new Error('No hay texto para leer');
            }

            // Si ya está hablando, pausar primero
            if (this.isSpeaking) {
                this.pause();
                if (this.currentText === text) {
                    return; // Ya se está leyendo este texto
                }
            }
            
            this.stop(); // Detener cualquier lectura previa
            this.currentText = text;
            
            // Resaltar elemento si se proporciona
            if (element) {
                this.highlightElement(element);
            }
            
            // Crear y configurar el discurso
            this.speechUtterance = new SpeechSynthesisUtterance(text);
            
            // Configurar voz
            if (this.settings.voice) {
                this.speechUtterance.voice = this.settings.voice;
            }
            
            // Configurar parámetros de voz
            this.speechUtterance.rate = Math.max(0.1, Math.min(2, parseFloat(this.settings.rate) || 1.0));
            this.speechUtterance.pitch = Math.max(0.1, Math.min(2, parseFloat(this.settings.pitch) || 1.0));
            this.speechUtterance.volume = Math.max(0, Math.min(1, parseFloat(this.settings.volume) || 1.0));
            this.speechUtterance.lang = 'es-ES';
            
            // Manejar eventos
            this.speechUtterance.onstart = () => {
                console.log('Iniciando lectura...');
                this.isSpeaking = true;
                this.updatePlayPauseButton(true);
            };
            
            this.speechUtterance.onend = () => {
                console.log('Lectura finalizada');
                this.isSpeaking = false;
                this.updatePlayPauseButton(false);
                this.removeHighlight();
            };
            
            this.speechUtterance.onerror = (event) => {
                console.error('Error en la síntesis de voz:', event);
                this.isSpeaking = false;
                this.updatePlayPauseButton(false);
                this.removeHighlight();
                
                // Mostrar mensaje de error al usuario
                if (event.error === 'interrupted') {
                    console.log('Lectura interrumpida por el usuario');
                } else {
                    alert('Error al reproducir el texto. Asegúrate de tener instaladas las voces necesarias.');
                }
            };
            
            // Iniciar la lectura
            console.log('Iniciando síntesis de voz...');
            this.speechSynthesis.speak(this.speechUtterance);
            
        } catch (error) {
            console.error('Error en la función speak:', error);
            alert('Error al intentar leer el texto: ' + error.message);
        }
    }
    
    pause() {
        if (this.isSpeaking) {
            this.speechSynthesis.pause();
            this.isSpeaking = false;
            this.updatePlayPauseButton(false);
        }
    }
    
    resume() {
        if (!this.isSpeaking && this.speechSynthesis.paused) {
            this.speechSynthesis.resume();
            this.isSpeaking = true;
            this.updatePlayPauseButton(true);
        }
    }
    
    stop() {
        this.speechSynthesis.cancel();
        this.isSpeaking = false;
        this.currentText = '';
        this.updatePlayPauseButton(false);
        this.removeHighlight();
    }
    
    toggle() {
        try {
            console.log('toggle() llamado');
            
            // Verificar si el navegador soporta la síntesis de voz
            if (!('speechSynthesis' in window)) {
                throw new Error('Tu navegador no soporta la síntesis de voz');
            }
            
            // Si está pausado, reanudar
            if (this.speechSynthesis.paused) {
                console.log('Reanudando lectura...');
                this.resume();
                return;
            }
            
            // Si está hablando, pausar
            if (this.isSpeaking) {
                console.log('Pausando lectura...');
                this.pause();
                return;
            }
            
            // Si hay texto actual, reproducirlo
            if (this.currentText) {
                console.log('Reproduciendo texto actual...');
                this.speak(this.currentText);
                return;
            }
            
            // Si no hay texto actual, intentar leer la página
            console.log('Intentando leer el contenido de la página...');
            const pageContent = readPageContent();
            if (pageContent) {
                console.log('Contenido de la página listo para leer');
                this.speak(pageContent);
            } else {
                console.warn('No se pudo encontrar contenido para leer');
                // Intentar leer el body como último recurso
                const bodyText = document.body.innerText.trim();
                if (bodyText) {
                    console.log('Leyendo contenido del body...');
                    this.speak(bodyText);
                } else {
                    throw new Error('No se encontró ningún texto para leer en la página');
                }
            }
            
        } catch (error) {
            console.error('Error en toggle():', error);
            
            // Mostrar mensaje de error al usuario
            const errorMessage = error.message || 'Error al intentar leer el contenido';
            
            // Verificar si el error es por falta de voces
            if (errorMessage.includes('voice') || errorMessage.includes('voz')) {
                alert('No se encontraron voces disponibles. Por favor, verifica la configuración de idioma de tu sistema.');
            } else {
                alert(errorMessage);
            }
            
            // Restablecer el estado
            this.isSpeaking = false;
            this.updatePlayPauseButton(false);
        }
    }
    
    highlightElement(element) {
        this.removeHighlight();
        this.currentElement = element;
        element.classList.add('tts-highlight');
        
        // Desplazar el elemento a la vista
        element.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
    
    removeHighlight() {
        if (this.currentElement) {
            this.currentElement.classList.remove('tts-highlight');
            this.currentElement = null;
        }
    }
    
    updatePlayPauseButton(isPlaying) {
        const playPauseBtn = document.getElementById('tts-play-pause');
        if (playPauseBtn) {
            const icon = playPauseBtn.querySelector('i');
            if (icon) {
                icon.className = isPlaying ? 'fas fa-pause' : 'fas fa-play';
            }
        }
    }
    
    // Métodos para actualizar configuraciones
    setVoice(voiceIndex) {
        if (this.voices && voiceIndex >= 0 && voiceIndex < this.voices.length) {
            this.settings.voice = this.voices[voiceIndex];
            this.settings.lang = this.voices[voiceIndex].lang;
            this.saveSettings();
        }
    }
    
    setRate(rate) {
        this.settings.rate = parseFloat(rate);
        this.saveSettings();
    }
    
    setPitch(pitch) {
        this.settings.pitch = parseFloat(pitch);
        this.saveSettings();
    }
    
    setVolume(volume) {
        this.settings.volume = parseFloat(volume);
        this.saveSettings();
    }
}

// Inicializar TTS cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', () => {
    // Mostrar mensaje de depuración
    console.log('Inicializando Text-to-Speech...');

    // Inicializar el objeto TTS
    if (!window.tts) {
        window.tts = new TextToSpeech();
    }

    // Obtener referencias a los elementos del DOM
    const playPauseBtn = document.getElementById('tts-play-pause');
    const stopBtn = document.getElementById('tts-stop');
    const rateInput = document.getElementById('tts-rate');
    const rateValue = document.getElementById('tts-rate-value');
    const pitchInput = document.getElementById('tts-pitch');
    const pitchValue = document.getElementById('tts-pitch-value');
    const volumeInput = document.getElementById('tts-volume');
    const volumeValue = document.getElementById('tts-volume-value');
    const voiceSelect = document.getElementById('tts-voice');

    // Función para leer el contenido de la página
    const readPageContent = () => {
        console.log('Leyendo contenido de la página...');
        try {
            // Excluir elementos que no queremos leer
            const excludedSelectors = [
                'script', 'style', 'noscript', 'iframe', 'nav', 'header', 'footer',
                '.sr-only', '.visually-hidden', '.hidden', '[aria-hidden="true"]',
                '.accessibility-panel', '#accessibility-panel'
            ];
            
            // Clonar el body para no afectar el DOM original
            const clone = document.documentElement.cloneNode(true);
            
            // Eliminar elementos excluidos
            excludedSelectors.forEach(selector => {
                const elements = clone.querySelectorAll(selector);
                elements.forEach(el => el.remove());
            });
            
            // Obtener el contenido de texto limpio
            let textToRead = '';
            
            // Intentar obtener el contenido principal de la página
            const mainContent = clone.querySelector('main') || 
                              clone.querySelector('[role="main"]') || 
                              clone.querySelector('.container') || 
                              clone.querySelector('.content') || 
                              clone.body;
            
            if (mainContent) {
                // Limpiar el texto: eliminar espacios en blanco múltiples y saltos de línea
                textToRead = mainContent.innerText
                    .replace(/\s+/g, ' ')
                    .replace(/\n+/g, '\n')
                    .trim();
            }
            
            if (textToRead && textToRead.length > 0) {
                console.log('Texto a leer encontrado');
                
                // Si hay mucho texto, dividirlo en partes más pequeñas
                const maxLength = 10000; // Máximo de caracteres por fragmento
                if (textToRead.length > maxLength) {
                    console.log('Texto muy largo, dividiendo en fragmentos...');
                    const chunks = [];
                    for (let i = 0; i < textToRead.length; i += maxLength) {
                        chunks.push(textToRead.substring(i, i + maxLength));
                    }
                    
                    // Reproducir el primer fragmento y encolar los demás
                    if (chunks.length > 0) {
                        window.tts.speak(chunks[0]);
                        
                        // Encolar los fragmentos restantes
                        for (let i = 1; i < chunks.length; i++) {
                            setTimeout(() => {
                                if (window.tts) {
                                    window.tts.speak(chunks[i]);
                                }
                            }, i * 10000); // 10 segundos entre fragmentos
                        }
                    }
                } else {
                    window.tts.speak(textToRead);
                }
                
                // Actualizar el botón a "Pausar"
                if (playPauseBtn) {
                    const icon = playPauseBtn.querySelector('i');
                    if (icon) {
                        icon.className = 'fas fa-pause';
                    }
                    const span = playPauseBtn.querySelector('span');
                    if (span) {
                        span.textContent = ' Pausar';
                    }
                }
                
                return textToRead;
            } else {
                console.warn('No se encontró texto para leer en la página actual');
                return null;
            }
        } catch (error) {
            console.error('Error al leer el contenido de la página:', error);
            return null;
        }
    };
    
    // Actualizar controles de la interfaz
    function updateUIControls() {
        // Cargar voces disponibles
        if (voiceSelect) {
            // Limpiar opciones existentes
            voiceSelect.innerHTML = '';
            
            // Obtener voces disponibles
            const voices = window.speechSynthesis.getVoices();
            
            if (voices.length === 0) {
                // Si no hay voces disponibles, intentar de nuevo más tarde
                setTimeout(updateUIControls, 100);
                return;
            }
            
            // Agregar opción por defecto
            const defaultOption = document.createElement('option');
            defaultOption.value = '';
            defaultOption.textContent = 'Seleccionar voz...';
            voiceSelect.appendChild(defaultOption);
            
            // Agregar voces disponibles
            voices.forEach((voice, index) => {
                const option = document.createElement('option');
                option.value = index;
                option.textContent = `${voice.name} (${voice.lang})`;
                voiceSelect.appendChild(option);
            });
            
            // Seleccionar voz predeterminada en español si está disponible
            const spanishVoice = voices.findIndex(v => v.lang.startsWith('es'));
            if (spanishVoice !== -1) {
                voiceSelect.value = spanishVoice;
            }
        }
    }
    
    // Event Listeners
    if (playPauseBtn) {
        playPauseBtn.addEventListener('click', (e) => {
            e.preventDefault();
            console.log('Botón de reproducción/pausa clickeado');
            
            if (!window.tts.isSpeaking || !window.tts.currentText) {
                // Si no se está reproduciendo nada o no hay texto actual
                readPageContent();
            } else {
                // Si ya se está reproduciendo, pausar o reanudar
                window.tts.toggle();
                
                // Actualizar ícono del botón
                const icon = playPauseBtn.querySelector('i');
                if (icon) {
                    if (window.tts.isPaused) {
                        icon.className = 'fas fa-play';
                        playPauseBtn.querySelector('span').textContent = ' Reanudar';
                    } else {
                        icon.className = 'fas fa-pause';
                        playPauseBtn.querySelector('span').textContent = ' Pausar';
                    }
                }
            }
        });
    }
    
    if (stopBtn) {
        stopBtn.addEventListener('click', (e) => {
            e.preventDefault();
            console.log('Botón de detener clickeado');
            
            // Detener la reproducción
            window.tts.stop();
            
            // Restaurar el botón de reproducción
            if (playPauseBtn) {
                const icon = playPauseBtn.querySelector('i');
                if (icon) {
                    icon.className = 'fas fa-play';
                }
                playPauseBtn.querySelector('span').textContent = ' Leer página';
            }
        });
    }
    
    // Actualizar controles de velocidad, tono y volumen
    if (rateInput) {
        rateInput.addEventListener('input', (e) => {
            const value = parseFloat(e.target.value);
            window.tts.setRate(value);
            if (rateValue) rateValue.textContent = value.toFixed(1);
        });
    }
    
    if (pitchInput) {
        pitchInput.addEventListener('input', (e) => {
            const value = parseFloat(e.target.value);
            window.tts.setPitch(value);
            if (pitchValue) pitchValue.textContent = value.toFixed(1);
        });
    }
    
    if (volumeInput) {
        volumeInput.addEventListener('input', (e) => {
            const value = parseFloat(e.target.value);
            window.tts.setVolume(value);
            if (volumeValue) volumeValue.textContent = value.toFixed(1);
        });
    }
    
    if (voiceSelect) {
        voiceSelect.addEventListener('change', (e) => {
            const voiceIndex = parseInt(e.target.value);
            if (!isNaN(voiceIndex)) {
                const voices = window.speechSynthesis.getVoices();
                if (voices[voiceIndex]) {
                    window.tts.setVoice(voices[voiceIndex]);
                }
            }
        });
    }
    
    // Cargar voces cuando estén disponibles
    if (window.speechSynthesis.onvoiceschanged !== undefined) {
        window.speechSynthesis.onvoiceschanged = updateUIControls;
    }
    
    // Inicializar controles
    updateUIControls();
    
    console.log('Text-to-Speech inicializado correctamente');
});
