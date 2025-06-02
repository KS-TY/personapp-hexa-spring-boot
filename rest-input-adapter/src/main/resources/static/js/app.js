// PersonApp - JavaScript Application

const PERSONAS_API = '/api/v1/persona';
const PROFESSIONS_API = '/api/v1/profession';
const PHONES_API = '/api/v1/phone';
const STUDIES_API = '/api/v1/study';

// Variables globales
let currentEditId = null;
let currentDeleteId = null;
let currentDeleteDatabase = null;
let currentEditDatabase = null; 
let currentSection = 'personas';


// Configuración de APIs por sección
const APIS = {
    personas: PERSONAS_API,
    professions: PROFESSIONS_API,
    phones: PHONES_API,
    studies: STUDIES_API
};

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

function initializeApp() {
    setupNavigation();
    showSection('personas');
    loadBothDatabases();
    setupEventListeners();
}

// Navegación
function setupNavigation() {
    document.querySelectorAll('.nav-button').forEach(button => {
        button.addEventListener('click', function() {
            const section = this.dataset.section;
            showSection(section);
        });
    });
}

function showSection(section) {
    console.log('Switching to section:', section);
    currentSection = section;
    
    // Actualizar navegación
    document.querySelectorAll('.nav-button').forEach(btn => {
        btn.classList.remove('active');
    });
    document.querySelector(`[data-section="${section}"]`).classList.add('active');
    
    // Actualizar secciones
    document.querySelectorAll('.section').forEach(sec => {
        sec.classList.remove('active');
    });
    document.getElementById(`${section}-section`).classList.add('active');
    
    // Cargar datos de la sección
    loadBothDatabases();
    
    // Cargar datos específicos según la sección
    if (section === 'phones') {
        setTimeout(() => loadAvailableOwners(), 500);
    } else if (section === 'studies') {
        setTimeout(() => loadAvailablePersonsAndProfessions(), 500);
    }
}

// Event Listeners
function setupEventListeners() {
    // Tecla Enter en formularios
    document.addEventListener('keypress', function(e) {
        if (e.key === 'Enter' && e.target.tagName === 'INPUT') {
            if (document.getElementById('editModal').style.display === 'block') {
                updateItem();
            } else {
                createItem();
            }
        }
    });

    // Cerrar modal al hacer clic fuera
    window.onclick = function(event) {
        const editModal = document.getElementById('editModal');
        const confirmModal = document.getElementById('confirmModal');
        
        if (event.target === editModal) {
            closeEditModal();
        }
        if (event.target === confirmModal) {
            closeConfirmModal();
        }
    }
}

// Utilidades
function showMessage(message, type = 'info') {
    const messagesDiv = document.getElementById('messages');
    const messageElement = document.createElement('div');
    messageElement.className = type;
    messageElement.textContent = message;
    messagesDiv.appendChild(messageElement);
    
    setTimeout(() => {
        messageElement.remove();
    }, 5000);
}

function setLoading(show) {
    document.getElementById('loading').style.display = show ? 'block' : 'none';
}

function getItemId(item) {
    console.log('Getting ID from item:', item);
    const id = item._id || item.id || item.dni || item.identification || item.cc;
    
    if (!id || id === 'null' || id === 'undefined' || id === '' || String(id).trim() === '') {
        console.error('Invalid ID found:', id);
        return null;
    }
    
    return String(id).trim();
}

// Función corregida para cargar elementos según la sección activa
async function loadItems() {
    // Usar el selector de base de datos correcto según la sección
    let database;
    if (currentSection === 'personas') {
        database = document.getElementById('database').value;
    } else if (currentSection === 'professions') {
        database = document.getElementById('professionDatabase').value;
    } else if (currentSection === 'phones') {
        database = document.getElementById('phoneDatabase').value;
    } else if (currentSection === 'studies') {
        database = document.getElementById('studyDatabase').value;
    }
    
    const api = APIS[currentSection];
    setLoading(true);
    
    try {
        const response = await fetch(`${api}/${database}`);
        if (!response.ok) {
            throw new Error(`Error ${response.status}: ${response.statusText}`);
        }
        
        const items = await response.json();
        console.log(`Items loaded from ${database}:`, items);
        
        displayItems(items, database);
        updateStats(items, database);
        showMessage(`✅ Cargados ${items.length} elementos desde ${database}`, 'success');
    } catch (error) {
        console.error('Error:', error);
        showMessage(`❌ Error al cargar datos: ${error.message}`, 'error');
    } finally {
        setLoading(false);
    }
}

async function loadBothDatabases() {
    const api = APIS[currentSection];
    setLoading(true);
    
    try {
        const [mariaResponse, mongoResponse] = await Promise.all([
            fetch(`${api}/MARIA`),
            fetch(`${api}/MONGO`)
        ]);

        const mariaItems = mariaResponse.ok ? await mariaResponse.json() : [];
        const mongoItems = mongoResponse.ok ? await mongoResponse.json() : [];
        
        console.log(`=== MARIA RESPONSE ${currentSection} ===`, mariaItems);
        console.log(`=== MONGO RESPONSE ${currentSection} ===`, mongoItems);
        
        const allItems = [
            ...mariaItems.map(item => ({...item, database: 'MariaDB'})),
            ...mongoItems.map(item => ({...item, database: 'MongoDB'}))
        ];
        
        console.log(`=== COMBINED ${currentSection.toUpperCase()} ===`, allItems);
        
        displayItems(allItems, 'BOTH');
        updateStatsFromBoth(mariaItems, mongoItems);
        showMessage(`✅ Cargados ${allItems.length} elementos total (${mariaItems.length} MariaDB, ${mongoItems.length} MongoDB)`, 'success');
    } catch (error) {
        console.error('Error:', error);
        showMessage(`❌ Error al cargar datos: ${error.message}`, 'error');
    } finally {
        setLoading(false);
    }
}

// Mostrar elementos
function displayItems(items, source) {
    console.log(`=== DISPLAY ${currentSection.toUpperCase()} ===`);
    console.log('Items array:', items);
    console.log('Source:', source);

    const container = document.getElementById(`${currentSection}-list`);
    
    if (!items || items.length === 0) {
        container.innerHTML = `<p style="text-align: center; color: #666; padding: 40px;">No hay ${currentSection} registradas</p>`;
        return;
    }
    
    if (currentSection === 'personas') {
        displayPersonas(items, container);
    } else if (currentSection === 'professions') {
        displayProfessions(items, container);
    } else if (currentSection === 'phones') {
        displayPhones(items, container);
    } else if (currentSection === 'studies') {
        displayStudies(items, container);
    }
}

function displayPersonas(personas, container) {
    container.innerHTML = personas.map(person => {
        const personId = getItemId(person);
        if (!personId || personId === 'undefined' || personId === 'null') {
            console.error('Invalid person ID:', person);
            return '';
        }
        
        const personFirstName = (person.firstName || person.nombre || 'Sin nombre').replace(/'/g, "&apos;");
        const personLastName = (person.lastName || person.apellido || 'Sin apellido').replace(/'/g, "&apos;");
        const personAge = person.age || person.edad || 'No especificada';
        const personSex = person.sex || person.genero || 'M';
        const personDatabase = person.database || 'MariaDB';
        
        return `
            <div class="item-card">
                <div class="item-id">ID: ${personId}</div>
                <div class="item-name">${personFirstName} ${personLastName}</div>
                <div class="item-details">
                    <strong>Edad:</strong> ${personAge}<br>
                    <strong>Género:</strong> ${getGenderText(personSex)}
                </div>
                <span class="database-badge ${personDatabase === 'MariaDB' ? 'maria-badge' : 'mongo-badge'}">
                    ${personDatabase}
                </span>
                <div class="item-actions">
                    <button class="btn-small btn-edit" onclick="openPersonaEditModal('${personId}', '${personFirstName}', '${personLastName}', '${personAge}', '${personSex}', '${personDatabase}')">
                        ✏️ Editar
                    </button>
                    <button class="btn-small btn-delete" onclick="handleDeleteClick('${personId}', '${personDatabase}')">
                        🗑️ Eliminar
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

function displayProfessions(professions, container) {
    container.innerHTML = professions.map(profession => {
        const professionId = getItemId(profession);
        if (!professionId || professionId === 'undefined' || professionId === 'null') {
            console.error('Invalid profession ID:', profession);
            return '';
        }
        
        const professionName = (profession.name || profession.nom || 'Sin nombre').replace(/'/g, "&apos;");
        const professionDescription = (profession.description || profession.des || 'Sin descripción').replace(/'/g, "&apos;");
        const professionDatabase = profession.database || 'MariaDB';
        
        return `
            <div class="item-card">
                <div class="item-id">ID: ${professionId}</div>
                <div class="item-name">${professionName}</div>
                <div class="item-details">
                    <strong>Descripción:</strong> ${professionDescription}
                </div>
                <span class="database-badge ${professionDatabase === 'MariaDB' ? 'maria-badge' : 'mongo-badge'}">
                    ${professionDatabase}
                </span>
                <div class="item-actions">
                    <button class="btn-small btn-edit" onclick="openProfessionEditModal('${professionId}', '${professionName}', '${professionDescription}', '${professionDatabase}')">
                        ✏️ Editar
                    </button>
                    <button class="btn-small btn-delete" onclick="handleDeleteClick('${professionId}', '${professionDatabase}')">
                        🗑️ Eliminar
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

// Función específica para manejar eliminación de estudios
function handleStudyDeleteClick(personId, professionId, database) {
    console.log('=== HANDLE STUDY DELETE CLICK ===');
    console.log('Received PersonId:', personId, 'ProfessionId:', professionId, 'Database:', database);
    
    if (!personId || personId === 'null' || personId === 'undefined' || personId === 'N/A') {
        console.error('Invalid PersonId for delete:', personId);
        showMessage('❌ Error: ID de persona no válido para eliminar', 'error');
        return;
    }
    
    if (!professionId || professionId === 'null' || professionId === 'undefined' || professionId === 'N/A') {
        console.error('Invalid ProfessionId for delete:', professionId);
        showMessage('❌ Error: ID de profesión no válido para eliminar', 'error');
        return;
    }
    
    if (!database || database === 'null' || database === 'undefined') {
        console.error('Invalid database for delete:', database);
        showMessage('❌ Error: Base de datos no válida para eliminar', 'error');
        return;
    }
    
    // Establecer las variables globales para eliminación
    currentDeleteId = `${personId}-${professionId}`;
    currentDeleteDatabase = database === 'MariaDB' ? 'MARIA' : 'MONGO';
    
    console.log('Set currentDeleteId:', currentDeleteId);
    console.log('Set currentDeleteDatabase:', currentDeleteDatabase);
    
    const message = `¿Está seguro de que desea eliminar el estudio de la persona ${personId} en la profesión ${professionId} de ${database}?`;
    document.getElementById('confirmMessage').textContent = message;
    document.getElementById('confirmModal').style.display = 'block';
}



function displayPhones(phones, container) {
    container.innerHTML = phones.map(phone => {
        const phoneNumber = getItemId(phone) || phone.number || phone.num || 'Sin número';
        if (!phoneNumber || phoneNumber === 'undefined' || phoneNumber === 'null') {
            console.error('Invalid phone number:', phone);
            return '';
        }
        
        const phoneCompany = (phone.company || phone.oper || 'Sin compañía').replace(/'/g, "&apos;");
        const phoneOwnerId = phone.ownerId || phone.duenio || 'Sin propietario';
        const phoneDatabase = phone.database || 'MariaDB';
        
        return `
            <div class="item-card">
                <div class="item-id">📱 ${phoneNumber}</div>
                <div class="item-name">${phoneCompany}</div>
                <div class="item-details">
                    <strong>Propietario ID:</strong> ${phoneOwnerId}
                </div>
                <span class="database-badge ${phoneDatabase === 'MariaDB' ? 'maria-badge' : 'mongo-badge'}">
                    ${phoneDatabase}
                </span>
                <div class="item-actions">
                    <button class="btn-small btn-edit" onclick="openPhoneEditModal('${phoneNumber}', '${phoneCompany}', '${phoneOwnerId}', '${phoneDatabase}')">
                        ✏️ Editar
                    </button>
                    <button class="btn-small btn-delete" onclick="handleDeleteClick('${phoneNumber}', '${phoneDatabase}')">
                        🗑️ Eliminar
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

function displayStudies(studies, container) {
    container.innerHTML = studies.map(study => {
        // Obtener IDs de manera más robusta
        const personId = study.personId || study.cc_per || study._id?.split('-')[0] || 'N/A';
        const professionId = study.professionId || study.id_prof || study._id?.split('-')[1] || 'N/A';
        
        const studyId = `${personId}-${professionId}`;
        const graduationDate = (study.graduationDate || study.fecha || 'No especificada').replace(/'/g, "&apos;");
        const universityName = (study.universityName || study.univer || 'No especificada').replace(/'/g, "&apos;");
        const studyDatabase = study.database || 'MariaDB';
        
        console.log('Study item:', {personId, professionId, graduationDate, universityName, studyDatabase});
        
        return `
            <div class="item-card">
                <div class="item-id">👨‍🎓 ${studyId}</div>
                <div class="item-name">Persona ID: ${personId} → Profesión ID: ${professionId}</div>
                <div class="item-details">
                    <strong>Fecha de graduación:</strong> ${graduationDate}<br>
                    <strong>Universidad:</strong> ${universityName}
                </div>
                <span class="database-badge ${studyDatabase === 'MariaDB' ? 'maria-badge' : 'mongo-badge'}">
                    ${studyDatabase}
                </span>
                <div class="item-actions">
                    <button class="btn-small btn-edit" onclick="openStudyEditModal('${personId}', '${professionId}', '${graduationDate}', '${universityName}', '${studyDatabase}')">
                        ✏️ Editar
                    </button>
                    <button class="btn-small btn-delete" onclick="handleStudyDeleteClick('${personId}', '${professionId}', '${studyDatabase}')">
                        🗑️ Eliminar
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

function getGenderText(sex) {
    switch(sex) {
        case 'M': case 'MALE': return 'Masculino';
        case 'F': case 'FEMALE': return 'Femenino';
        case 'O': case 'OTHER': return 'Otro';
        default: return 'No especificado';
    }
}

// Estadísticas
function updateStats(items, database) {
    const total = items.length;
    
    if (currentSection === 'personas') {
        document.getElementById('totalPersonas').textContent = total;
        if (database === 'MARIA') {
            document.getElementById('personasMariaCount').textContent = total;
            document.getElementById('personasMongoCount').textContent = '0';
        } else if (database === 'MONGO') {
            document.getElementById('personasMongoCount').textContent = total;
            document.getElementById('personasMariaCount').textContent = '0';
        }
    } else if (currentSection === 'professions') {
        document.getElementById('totalProfessions').textContent = total;
        if (database === 'MARIA') {
            document.getElementById('professionsMariaCount').textContent = total;
            document.getElementById('professionsMongoCount').textContent = '0';
        } else if (database === 'MONGO') {
            document.getElementById('professionsMongoCount').textContent = total;
            document.getElementById('professionsMariaCount').textContent = '0';
        }
    } else if (currentSection === 'phones') {
        document.getElementById('totalPhones').textContent = total;
        if (database === 'MARIA') {
            document.getElementById('phonesMariaCount').textContent = total;
            document.getElementById('phonesMongoCount').textContent = '0';
        } else if (database === 'MONGO') {
            document.getElementById('phonesMongoCount').textContent = total;
            document.getElementById('phonesMariaCount').textContent = '0';
        }
    } else if (currentSection === 'studies') {
        document.getElementById('totalStudies').textContent = total;
        if (database === 'MARIA') {
            document.getElementById('studiesMariaCount').textContent = total;
            document.getElementById('studiesMongoCount').textContent = '0';
        } else if (database === 'MONGO') {
            document.getElementById('studiesMongoCount').textContent = total;
            document.getElementById('studiesMariaCount').textContent = '0';
        }
    }
}

function updateStatsFromBoth(mariaItems, mongoItems) {
    if (currentSection === 'personas') {
        document.getElementById('personasMariaCount').textContent = mariaItems.length;
        document.getElementById('personasMongoCount').textContent = mongoItems.length;
        document.getElementById('totalPersonas').textContent = mariaItems.length + mongoItems.length;
    } else if (currentSection === 'professions') {
        document.getElementById('professionsMariaCount').textContent = mariaItems.length;
        document.getElementById('professionsMongoCount').textContent = mongoItems.length;
        document.getElementById('totalProfessions').textContent = mariaItems.length + mongoItems.length;
    } else if (currentSection === 'phones') {
        document.getElementById('phonesMariaCount').textContent = mariaItems.length;
        document.getElementById('phonesMongoCount').textContent = mongoItems.length;
        document.getElementById('totalPhones').textContent = mariaItems.length + mongoItems.length;
    } else if (currentSection === 'studies') {
        document.getElementById('studiesMariaCount').textContent = mariaItems.length;
        document.getElementById('studiesMongoCount').textContent = mongoItems.length;
        document.getElementById('totalStudies').textContent = mariaItems.length + mongoItems.length;
    }
}

// Crear elemento - FUNCIÓN SIMPLIFICADA Y CORREGIDA
async function createItem() {
    console.log('=== CREATE ITEM START ===');
    console.log('Current section:', currentSection);
    
    const api = APIS[currentSection];
    const formData = {};
    
    // Recopilar datos del formulario según la sección
    if (currentSection === 'personas') {
        formData.dni = document.getElementById('dni').value;
        formData.firstName = document.getElementById('firstName').value;
        formData.lastName = document.getElementById('lastName').value;
        formData.age = document.getElementById('age').value;
        formData.sex = document.getElementById('sex').value;
        formData.database = document.getElementById('personCreateDatabase').value;
        
        if (!formData.dni || !formData.firstName || !formData.lastName) {
            showMessage('❌ Por favor complete los campos obligatorios (ID, Nombre, Apellido)', 'error');
            return;
        }
    } else if (currentSection === 'professions') {
        formData.id = document.getElementById('professionId').value;
        formData.name = document.getElementById('professionName').value;
        formData.description = document.getElementById('professionDescription').value;
        formData.database = document.getElementById('professionCreateDatabase').value;
        
        if (!formData.id || !formData.name) {
            showMessage('❌ Por favor complete los campos obligatorios (ID, Nombre)', 'error');
            return;
        }
    } else if (currentSection === 'phones') {
        formData.number = document.getElementById('phoneNumber').value;
        formData.company = document.getElementById('phoneCompany').value;
        formData.ownerId = document.getElementById('phoneOwnerId').value;
        formData.database = document.getElementById('phoneCreateDatabase').value;
        
        if (!formData.number || !formData.company || !formData.ownerId || !formData.database) {
            showMessage('❌ Por favor complete todos los campos obligatorios', 'error');
            return;
        }
    } else if (currentSection === 'studies') {
        formData.personId = document.getElementById('studyPersonId').value;
        formData.professionId = document.getElementById('studyProfessionId').value;
        formData.graduationDate = document.getElementById('studyGraduationDate').value;
        formData.universityName = document.getElementById('studyUniversityName').value;
        formData.database = document.getElementById('studyCreateDatabase').value;
        
        if (!formData.personId || !formData.professionId || !formData.database) {
            showMessage('❌ Por favor complete los campos obligatorios (Persona, Profesión, Base de Datos)', 'error');
            return;
        }
    }

    console.log('=== FINAL FORM DATA ===');
    console.log(JSON.stringify(formData, null, 2));

    try {
        setLoading(true);
        
        const response = await fetch(api, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        });

        console.log('=== API RESPONSE ===');
        console.log('Status:', response.status);
        console.log('StatusText:', response.statusText);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Error response:', errorText);
            throw new Error(`Error ${response.status}: ${errorText}`);
        }

        const result = await response.json();
        console.log('=== API RESULT ===');
        console.log(JSON.stringify(result, null, 2));
        
        if (result.status && result.status.startsWith('ERROR')) {
            throw new Error(result.status);
        }

        showMessage(`✅ Elemento creado exitosamente en ${formData.database}`, 'success');
        
        // Limpiar formulario
        if (currentSection === 'personas') {
            document.getElementById('dni').value = '';
            document.getElementById('firstName').value = '';
            document.getElementById('lastName').value = '';
            document.getElementById('age').value = '';
        } else if (currentSection === 'professions') {
            document.getElementById('professionId').value = '';
            document.getElementById('professionName').value = '';
            document.getElementById('professionDescription').value = '';
        } else if (currentSection === 'phones') {
            document.getElementById('phoneNumber').value = '';
            document.getElementById('phoneCompany').value = '';
            document.getElementById('phoneOwnerId').value = '';
        } else if (currentSection === 'studies') {
            document.getElementById('studyPersonId').value = '';
            document.getElementById('studyProfessionId').value = '';
            document.getElementById('studyGraduationDate').value = '';
            document.getElementById('studyUniversityName').value = '';
        }
        
        loadBothDatabases();
        
    } catch (error) {
        console.error('=== CREATE ERROR ===');
        console.error('Error:', error);
        showMessage(`❌ Error al crear elemento: ${error.message}`, 'error');
    } finally {
        setLoading(false);
        console.log('=== CREATE ITEM END ===');
    }
}

// Función para cargar propietarios disponibles
async function loadAvailableOwners() {
    if (currentSection !== 'phones') return;
    
    try {
        // Cargar personas de ambas bases de datos
        const [mariaResponse, mongoResponse] = await Promise.all([
            fetch(`${PERSONAS_API}/MARIA`),
            fetch(`${PERSONAS_API}/MONGO`)
        ]);

        const mariaPersonas = mariaResponse.ok ? await mariaResponse.json() : [];
        const mongoPersonas = mongoResponse.ok ? await mongoResponse.json() : [];
        
        // Combinar y eliminar duplicados por ID
        const allPersonas = [...mariaPersonas, ...mongoPersonas];
        const uniquePersonas = allPersonas.filter((persona, index, self) => 
            index === self.findIndex(p => getItemId(p) === getItemId(persona))
        );
        
        // Actualizar el dropdown correcto
        updateOwnerDropdown(uniquePersonas);
        
    } catch (error) {
        console.error('Error loading available owners:', error);
        showMessage('❌ Error al cargar propietarios disponibles', 'error');
    }
}

function updateOwnerDropdown(personas) {
    const ownerSelect = document.getElementById('phoneOwnerId');
    if (!ownerSelect) {
        console.error('phoneOwnerId element not found');
        return;
    }
    
    // Limpiar opciones existentes
    ownerSelect.innerHTML = '<option value="">Seleccione un propietario...</option>';
    
    // Agregar personas disponibles
    personas.forEach(persona => {
        const personId = getItemId(persona);
        const personName = `${persona.firstName || persona.nombre || ''} ${persona.lastName || persona.apellido || ''}`.trim();
        
        if (personId && personName) {
            const option = document.createElement('option');
            option.value = personId;
            option.textContent = `${personId} - ${personName}`;
            ownerSelect.appendChild(option);
        }
    });
    
    console.log('Updated phone owner dropdown with', personas.length, 'persons');
}

// Modales de edición
function openPersonaEditModal(id, firstName, lastName, age, sex, database) {
    console.log('Opening persona edit modal with:', { id, firstName, lastName, age, sex, database });
    currentEditId = id;
    currentEditDatabase = database === 'MariaDB' ? 'MARIA' : 'MONGO';
    
    // Configurar el modal para personas
    document.getElementById('editModalTitle').textContent = '✏️ Editar Persona';
    document.getElementById('editPersonForm').style.display = 'block';
    document.getElementById('editProfessionForm').style.display = 'none';
    document.getElementById('editPhoneForm').style.display = 'none';
    
    // Llenar los campos
    document.getElementById('editDni').value = id;
    document.getElementById('editFirstName').value = firstName || '';
    document.getElementById('editLastName').value = lastName || '';
    document.getElementById('editAge').value = age && age !== 'null' && age !== 'undefined' && age !== 'No especificada' ? age : '';
    document.getElementById('editSex').value = sex || 'M';
    
    // Mostrar el modal
    document.getElementById('editModal').style.display = 'block';
}

function openProfessionEditModal(id, name, description, database) {
    console.log('Opening profession edit modal with:', { id, name, description, database });
    currentEditId = id;
    currentEditDatabase = database === 'MariaDB' ? 'MARIA' : 'MONGO';
    
    // Configurar el modal para profesiones
    document.getElementById('editModalTitle').textContent = '✏️ Editar Profesión';
    document.getElementById('editPersonForm').style.display = 'none';
    document.getElementById('editProfessionForm').style.display = 'block';
    document.getElementById('editPhoneForm').style.display = 'none';
    
    // Llenar los campos
    document.getElementById('editId').value = id;
    document.getElementById('editName').value = name || '';
    document.getElementById('editDescription').value = description || '';
    
    // Mostrar el modal
    document.getElementById('editModal').style.display = 'block';
}

function openPhoneEditModal(number, company, ownerId, database) {
    console.log('Opening phone edit modal with:', { number, company, ownerId, database });
    currentEditId = number;
    currentEditDatabase = database === 'MariaDB' ? 'MARIA' : 'MONGO';
    
    // Configurar el modal para teléfonos
    document.getElementById('editModalTitle').textContent = '✏️ Editar Teléfono';
    document.getElementById('editPersonForm').style.display = 'none';
    document.getElementById('editProfessionForm').style.display = 'none';
    document.getElementById('editPhoneForm').style.display = 'block';
    
    // Llenar los campos
    document.getElementById('editNumber').value = number;
    document.getElementById('editCompany').value = company || '';
    
    // Cargar opciones de propietarios en el dropdown de edición
    loadAvailableOwnersForEdit(ownerId);
    
    // Mostrar el modal
    document.getElementById('editModal').style.display = 'block';
}
function openStudyEditModal(personId, professionId, graduationDate, universityName, database) {
    console.log('Opening study edit modal with:', { personId, professionId, graduationDate, universityName, database });
    
    currentEditId = `${personId}-${professionId}`;
    currentEditDatabase = database === 'MariaDB' ? 'MARIA' : 'MONGO';
    
    console.log('Set currentEditId:', currentEditId);
    console.log('Set currentEditDatabase:', currentEditDatabase);
    
    // Configurar el modal para estudios
    document.getElementById('editModalTitle').textContent = '✏️ Editar Estudio';
    document.getElementById('editPersonForm').style.display = 'none';
    document.getElementById('editProfessionForm').style.display = 'none';
    document.getElementById('editPhoneForm').style.display = 'none';
    document.getElementById('editStudyForm').style.display = 'block';
    
    // Llenar los campos
    document.getElementById('editStudyPersonId').value = personId;
    document.getElementById('editStudyProfessionId').value = professionId;
    
    // Procesar fecha de graduación
    let formattedDate = '';
    if (graduationDate && graduationDate !== 'No especificada' && graduationDate !== 'null') {
        // Si viene en formato DD/MM/YYYY o similar, convertir a YYYY-MM-DD
        if (graduationDate.includes('/')) {
            const parts = graduationDate.split('/');
            if (parts.length === 3) {
                // Asumir DD/MM/YYYY
                formattedDate = `${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}`;
            }
        } else if (graduationDate.includes('-')) {
            // Ya está en formato YYYY-MM-DD o similar
            formattedDate = graduationDate.split('T')[0]; // Remover hora si existe
        } else {
            formattedDate = graduationDate;
        }
    }
    
    document.getElementById('editStudyGraduationDate').value = formattedDate;
    document.getElementById('editStudyUniversityName').value = universityName !== 'No especificada' ? universityName : '';
    
    // Mostrar el modal
    document.getElementById('editModal').style.display = 'block';
}


async function loadAvailableOwnersForEdit(selectedOwnerId) {
    try {
        // Cargar personas de ambas bases de datos
        const [mariaResponse, mongoResponse] = await Promise.all([
            fetch(`${PERSONAS_API}/MARIA`),
            fetch(`${PERSONAS_API}/MONGO`)
        ]);

        const mariaPersonas = mariaResponse.ok ? await mariaResponse.json() : [];
        const mongoPersonas = mongoResponse.ok ? await mongoResponse.json() : [];
        
        // Combinar y eliminar duplicados por ID
        const allPersonas = [...mariaPersonas, ...mongoPersonas];
        const uniquePersonas = allPersonas.filter((persona, index, self) => 
            index === self.findIndex(p => getItemId(p) === getItemId(persona))
        );
        
        // Actualizar el dropdown de edición
        const editOwnerSelect = document.getElementById('editOwnerId');
        if (editOwnerSelect) {
            editOwnerSelect.innerHTML = '<option value="">Seleccione un propietario...</option>';
            
            uniquePersonas.forEach(persona => {
                const personId = getItemId(persona);
                const personName = `${persona.firstName || persona.nombre || ''} ${persona.lastName || persona.apellido || ''}`.trim();
                
                if (personId && personName) {
                    const option = document.createElement('option');
                    option.value = personId;
                    option.textContent = `${personId} - ${personName}`;
                    
                    // Seleccionar el propietario actual
                    if (personId === selectedOwnerId) {
                        option.selected = true;
                    }
                    
                    editOwnerSelect.appendChild(option);
                }
            });
        }
        
    } catch (error) {
        console.error('Error loading available owners for edit:', error);
        showMessage('❌ Error al cargar propietarios para edición', 'error');
    }
}

function closeEditModal() {
    document.getElementById('editModal').style.display = 'none';
    document.getElementById('editPersonForm').style.display = 'none';
    document.getElementById('editProfessionForm').style.display = 'none';
    document.getElementById('editPhoneForm').style.display = 'none';
    document.getElementById('editStudyForm').style.display = 'none';  // Nueva línea
    currentEditId = null;
    currentEditDatabase = null;
}

// Actualizar elemento
async function updateItem() {
    if (!currentEditId) {
        showMessage('❌ Error: No hay elemento seleccionado para editar', 'error');
        return;
    }

    if (!currentEditDatabase) {
        showMessage('❌ Error: No se puede determinar la base de datos original', 'error');
        return;
    }

    const api = APIS[currentSection];
    let formData = {};
    
    if (currentSection === 'personas') {
        formData.dni = currentEditId;
        formData.firstName = document.getElementById('editFirstName').value;
        formData.lastName = document.getElementById('editLastName').value;
        formData.age = document.getElementById('editAge').value;
        formData.sex = document.getElementById('editSex').value;
        formData.database = currentEditDatabase;
        
        if (!formData.firstName || !formData.lastName) {
            showMessage('❌ Por favor complete los campos obligatorios (Nombre, Apellido)', 'error');
            return;
        }
    } else if (currentSection === 'professions') {
        formData.id = currentEditId;
        formData.name = document.getElementById('editName').value;
        formData.description = document.getElementById('editDescription').value;
        formData.database = currentEditDatabase;
        
        if (!formData.name) {
            showMessage('❌ Por favor complete el campo obligatorio (Nombre)', 'error');
            return;
        }
    } else if (currentSection === 'phones') {
        formData.number = currentEditId;
        formData.company = document.getElementById('editCompany').value;
        formData.ownerId = document.getElementById('editOwnerId').value;
        formData.database = currentEditDatabase;
        
        if (!formData.company || !formData.ownerId) {
            showMessage('❌ Por favor complete los campos obligatorios (Compañía, Propietario)', 'error');
            return;
        }
    } else if (currentSection === 'studies') {
        // Para estudios, currentEditId tiene formato "personId-professionId"
        const [personId, professionId] = currentEditId.split('-');
        formData.personId = personId;
        formData.professionId = professionId;
        formData.graduationDate = document.getElementById('editStudyGraduationDate').value;
        formData.universityName = document.getElementById('editStudyUniversityName').value;
        formData.database = currentEditDatabase;
        
        console.log('Studies update formData:', formData);
    }

    try {
        setLoading(true);
        
        let updateUrl;
        
        // Manejar URL especial para estudios
        if (currentSection === 'studies') {
            const [personId, professionId] = currentEditId.split('-');
            updateUrl = `${api}/${personId}/${professionId}`;
        } else {
            updateUrl = `${api}/${currentEditId}`;
        }
        
        const response = await fetch(updateUrl, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Error ${response.status}: ${errorText}`);
        }

        const result = await response.json();
        
        if (result.status && result.status.startsWith('ERROR')) {
            throw new Error(result.status);
        }

        showMessage(`✅ Elemento actualizado exitosamente en ${formData.database}`, 'success');
        closeEditModal();
        loadBothDatabases();
        
    } catch (error) {
        console.error('Error:', error);
        showMessage(`❌ Error al actualizar elemento: ${error.message}`, 'error');
    } finally {
        setLoading(false);
    }
}

// Función para cargar personas y profesiones disponibles para estudios
async function loadAvailablePersonsAndProfessions() {
    if (currentSection !== 'studies') return;
    
    try {
        console.log('Loading available persons and professions for studies...');
        
        // Cargar personas de ambas bases de datos
        const [mariaPersonasResponse, mongoPersonasResponse] = await Promise.all([
            fetch(`${PERSONAS_API}/MARIA`),
            fetch(`${PERSONAS_API}/MONGO`)
        ]);

        const mariaPersonas = mariaPersonasResponse.ok ? await mariaPersonasResponse.json() : [];
        const mongoPersonas = mongoPersonasResponse.ok ? await mongoPersonasResponse.json() : [];
        
        // Cargar profesiones de ambas bases de datos
        const [mariaProfessionsResponse, mongoProfessionsResponse] = await Promise.all([
            fetch(`${PROFESSIONS_API}/MARIA`),
            fetch(`${PROFESSIONS_API}/MONGO`)
        ]);

        const mariaProfessions = mariaProfessionsResponse.ok ? await mariaProfessionsResponse.json() : [];
        const mongoProfessions = mongoProfessionsResponse.ok ? await mongoProfessionsResponse.json() : [];
        
        // Combinar y eliminar duplicados por ID
        const allPersonas = [...mariaPersonas, ...mongoPersonas];
        const uniquePersonas = allPersonas.filter((persona, index, self) => 
            index === self.findIndex(p => getItemId(p) === getItemId(persona))
        );
        
        const allProfessions = [...mariaProfessions, ...mongoProfessions];
        const uniqueProfessions = allProfessions.filter((profession, index, self) => 
            index === self.findIndex(p => getItemId(p) === getItemId(profession))
        );
        
        // Actualizar los dropdowns
        updatePersonsDropdown(uniquePersonas);
        updateProfessionsDropdown(uniqueProfessions);
        
        console.log(`Loaded ${uniquePersonas.length} unique persons and ${uniqueProfessions.length} unique professions`);
        
    } catch (error) {
        console.error('Error loading available persons and professions:', error);
        showMessage('❌ Error al cargar personas y profesiones disponibles', 'error');
    }
}

function updatePersonsDropdown(personas) {
    const personSelect = document.getElementById('studyPersonId');
    if (!personSelect) {
        console.error('studyPersonId element not found');
        return;
    }
    
    // Limpiar opciones existentes
    personSelect.innerHTML = '<option value="">Seleccione una persona...</option>';
    
    // Agregar personas disponibles
    personas.forEach(persona => {
        const personId = getItemId(persona);
        const personName = `${persona.firstName || persona.nombre || ''} ${persona.lastName || persona.apellido || ''}`.trim();
        
        if (personId && personName) {
            const option = document.createElement('option');
            option.value = personId;
            option.textContent = `${personId} - ${personName}`;
            personSelect.appendChild(option);
        }
    });
    
    console.log('Updated study person dropdown with', personas.length, 'persons');
}

function updateProfessionsDropdown(professions) {
    const professionSelect = document.getElementById('studyProfessionId');
    if (!professionSelect) {
        console.error('studyProfessionId element not found');
        return;
    }
    
    // Limpiar opciones existentes
    professionSelect.innerHTML = '<option value="">Seleccione una profesión...</option>';
    
    // Agregar profesiones disponibles
    professions.forEach(profession => {
        const professionId = getItemId(profession);
        const professionName = profession.name || profession.nom || '';
        
        if (professionId && professionName) {
            const option = document.createElement('option');
            option.value = professionId;
            option.textContent = `${professionId} - ${professionName}`;
            professionSelect.appendChild(option);
        }
    });
    
    console.log('Updated study profession dropdown with', professions.length, 'professions');
}

// Eliminación
function handleDeleteClick(id, database) {
    console.log('=== HANDLE DELETE CLICK ===');
    console.log('Received ID:', id, 'Type:', typeof id);
    console.log('Received Database:', database, 'Type:', typeof database);
    
    if (!id || id === 'null' || id === 'undefined' || String(id).trim() === '' || String(id).trim() === 'null') {
        console.error('Invalid ID for delete:', id);
        showMessage('❌ Error: ID de elemento no válido para eliminar', 'error');
        return;
    }
    
    if (!database || database === 'null' || database === 'undefined' || String(database).trim() === '' || String(database).trim() === 'null') {
        console.error('Invalid database for delete:', database);
        showMessage('❌ Error: Base de datos no válida para eliminar', 'error');
        return;
    }
    
    const cleanId = String(id).trim();
    
    console.log('Proceeding with delete - ID:', cleanId, 'Database:', database);
    deleteItem(cleanId, database);
}

function deleteItem(id, database) {
    console.log('=== DELETE ITEM ===');
    console.log('ID received:', id, 'Type:', typeof id);
    console.log('Database received:', database, 'Type:', typeof database);
    
    const cleanId = String(id).trim();
    const cleanDatabase = String(database).trim();
    
    if (!cleanId || cleanId === 'null' || cleanId === 'undefined') {
        console.error('Cannot set currentDeleteId - invalid value:', cleanId);
        showMessage('❌ Error: ID no válido', 'error');
        return;
    }
    
    if (!cleanDatabase || cleanDatabase === 'null' || cleanDatabase === 'undefined') {
        console.error('Cannot set currentDeleteDatabase - invalid value:', cleanDatabase);
        showMessage('❌ Error: Base de datos no válida', 'error');
        return;
    }
    
    currentDeleteId = cleanId;
    currentDeleteDatabase = cleanDatabase === 'MariaDB' ? 'MARIA' : 'MONGO';
    
    console.log('Global vars set - currentDeleteId:', currentDeleteId);
    console.log('Global vars set - currentDeleteDatabase:', currentDeleteDatabase);
    
    if (!currentDeleteId || !currentDeleteDatabase) {
        console.error('Failed to set global delete variables');
        showMessage('❌ Error: No se pudieron establecer los datos para eliminar', 'error');
        return;
    }
    
    let entityType;
    if (currentSection === 'personas') entityType = 'persona';
    else if (currentSection === 'professions') entityType = 'profesión';
    else if (currentSection === 'phones') entityType = 'teléfono';
    
    const message = `¿Está seguro de que desea eliminar ${entityType} con ID ${currentDeleteId} de ${cleanDatabase}?`;
    document.getElementById('confirmMessage').textContent = message;
    document.getElementById('confirmModal').style.display = 'block';
}

function deleteStudy(personId, professionId, database) {
    console.log('=== DELETE STUDY ===');
    console.log('PersonId received:', personId, 'Type:', typeof personId);
    console.log('ProfessionId received:', professionId, 'Type:', typeof professionId);
    console.log('Database received:', database, 'Type:', typeof database);
    
    const cleanPersonId = String(personId).trim();
    const cleanProfessionId = String(professionId).trim();
    const cleanDatabase = String(database).trim();
    
    if (!cleanPersonId || cleanPersonId === 'null' || cleanPersonId === 'undefined') {
        console.error('Cannot set currentDeleteId - invalid PersonId:', cleanPersonId);
        showMessage('❌ Error: ID de persona no válido', 'error');
        return;
    }
    
    if (!cleanProfessionId || cleanProfessionId === 'null' || cleanProfessionId === 'undefined') {
        console.error('Cannot set currentDeleteId - invalid ProfessionId:', cleanProfessionId);
        showMessage('❌ Error: ID de profesión no válido', 'error');
        return;
    }
    
    if (!cleanDatabase || cleanDatabase === 'null' || cleanDatabase === 'undefined') {
        console.error('Cannot set currentDeleteDatabase - invalid value:', cleanDatabase);
        showMessage('❌ Error: Base de datos no válida', 'error');
        return;
    }
    
    // Para estudios, usamos un ID compuesto
    currentDeleteId = `${cleanPersonId}-${cleanProfessionId}`;
    currentDeleteDatabase = cleanDatabase === 'MariaDB' ? 'MARIA' : 'MONGO';
    
    console.log('Global vars set - currentDeleteId:', currentDeleteId);
    console.log('Global vars set - currentDeleteDatabase:', currentDeleteDatabase);
    
    if (!currentDeleteId || !currentDeleteDatabase) {
        console.error('Failed to set global delete variables');
        showMessage('❌ Error: No se pudieron establecer los datos para eliminar', 'error');
        return;
    }
    
    const message = `¿Está seguro de que desea eliminar el estudio de la persona ${cleanPersonId} en la profesión ${cleanProfessionId} de ${cleanDatabase}?`;
    document.getElementById('confirmMessage').textContent = message;
    document.getElementById('confirmModal').style.display = 'block';
}

async function confirmDelete() {
    console.log('=== CONFIRM DELETE START ===');
    console.log('currentDeleteId at start:', currentDeleteId);
    console.log('currentDeleteDatabase at start:', currentDeleteDatabase);
    console.log('currentSection:', currentSection);
    
    if (!currentDeleteId || currentDeleteId === 'null' || currentDeleteId === 'undefined') {
        console.error('currentDeleteId is invalid:', currentDeleteId);
        showMessage('❌ Error: ID de eliminación perdido', 'error');
        closeConfirmModal();
        return;
    }
    
    if (!currentDeleteDatabase || currentDeleteDatabase === 'null' || currentDeleteDatabase === 'undefined') {
        console.error('currentDeleteDatabase is invalid:', currentDeleteDatabase);
        showMessage('❌ Error: Base de datos de eliminación perdida', 'error');
        closeConfirmModal();
        return;
    }
    
    try {
        setLoading(true);
        closeConfirmModal();

        const api = APIS[currentSection];
        let deleteUrl;
        
        // Manejar URL especial para estudios (requiere personId y professionId separados)
        if (currentSection === 'studies' && currentDeleteId.includes('-')) {
            const [personId, professionId] = currentDeleteId.split('-');
            deleteUrl = `${api}/${personId}/${professionId}?database=${currentDeleteDatabase}`;
            console.log('Studies delete URL:', deleteUrl);
        } else {
            deleteUrl = `${api}/${currentDeleteId}?database=${currentDeleteDatabase}`;
            console.log('Standard delete URL:', deleteUrl);
        }

        const response = await fetch(deleteUrl, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        console.log('DELETE response status:', response.status);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('DELETE error response:', errorText);
            throw new Error(`Error ${response.status}: ${errorText}`);
        }

        const result = await response.json();
        console.log('DELETE result:', result);
        
        if (result.status && result.status.startsWith('ERROR')) {
            throw new Error(result.status);
        }

        let entityType;
        if (currentSection === 'personas') entityType = 'Persona';
        else if (currentSection === 'professions') entityType = 'Profesión';
        else if (currentSection === 'phones') entityType = 'Teléfono';
        else if (currentSection === 'studies') entityType = 'Estudio';
        
        showMessage(`✅ ${entityType} con ID ${currentDeleteId} eliminada exitosamente`, 'success');
        
        loadBothDatabases();
        
    } catch (error) {
        console.error('Error during delete:', error);
        showMessage(`❌ Error al eliminar elemento: ${error.message}`, 'error');
    } finally {
        setLoading(false);
        currentDeleteId = null;
        currentDeleteDatabase = null;
        console.log('=== CONFIRM DELETE END - Variables cleared ===');
    }
}

function closeConfirmModal() {
    console.log('=== CLOSE CONFIRM MODAL ===');
    document.getElementById('confirmModal').style.display = 'none';
}

function cancelDelete() {
    console.log('=== CANCEL DELETE ===');
    currentDeleteId = null;
    currentDeleteDatabase = null;
    closeConfirmModal();
}

function updateStatsFromBothForStudies(mariaItems, mongoItems) {
    document.getElementById('studiesMariaCount').textContent = mariaItems.length;
    document.getElementById('studiesMongoCount').textContent = mongoItems.length;
    document.getElementById('totalStudies').textContent = mariaItems.length + mongoItems.length;
}

async function confirmDelete() {
    console.log('=== CONFIRM DELETE START ===');
    console.log('currentDeleteId at start:', currentDeleteId, 'Type:', typeof currentDeleteId);
    console.log('currentDeleteDatabase at start:', currentDeleteDatabase, 'Type:', typeof currentDeleteDatabase);
    
    if (!currentDeleteId || currentDeleteId === 'null' || currentDeleteId === 'undefined') {
        console.error('currentDeleteId is invalid:', currentDeleteId);
        showMessage('❌ Error: ID de eliminación perdido', 'error');
        closeConfirmModal();
        return;
    }
    
    if (!currentDeleteDatabase || currentDeleteDatabase === 'null' || currentDeleteDatabase === 'undefined') {
        console.error('currentDeleteDatabase is invalid:', currentDeleteDatabase);
        showMessage('❌ Error: Base de datos de eliminación perdida', 'error');
        closeConfirmModal();
        return;
    }
    
    try {
        setLoading(true);
        closeConfirmModal();

        const api = APIS[currentSection];
        const deleteUrl = `${api}/${currentDeleteId}?database=${currentDeleteDatabase}`;
        console.log('DELETE URL constructed:', deleteUrl);

        const response = await fetch(deleteUrl, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        console.log('DELETE response status:', response.status);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('DELETE error response:', errorText);
            throw new Error(`Error ${response.status}: ${errorText}`);
        }

        const result = await response.json();
        console.log('DELETE result:', result);
        
        if (result.status && result.status.startsWith('ERROR')) {
            throw new Error(result.status);
        }

        let entityType;
        if (currentSection === 'personas') entityType = 'Persona';
        else if (currentSection === 'professions') entityType = 'Profesión';
        else if (currentSection === 'phones') entityType = 'Teléfono';
        
        showMessage(`✅ ${entityType} con ID ${currentDeleteId} eliminada exitosamente`, 'success');
        
        loadBothDatabases();
        
    } catch (error) {
        console.error('Error during delete:', error);
        showMessage(`❌ Error al eliminar elemento: ${error.message}`, 'error');
    } finally {
        setLoading(false);
        currentDeleteId = null;
        currentDeleteDatabase = null;
        console.log('=== CONFIRM DELETE END - Variables cleared ===');
    }
}