// PersonApp - JavaScript Application

const PERSONAS_API = '/api/v1/persona';
const PROFESSIONS_API = '/api/v1/profession';
const PHONES_API = '/api/v1/phone';

// Variables globales
let currentEditId = null;
let currentDeleteId = null;
let currentDeleteDatabase = null;
let currentEditDatabase = null; // Nueva variable para recordar la BD original
let currentSection = 'personas';

// Configuración de APIs por sección
const APIS = {
    personas: PERSONAS_API,
    professions: PROFESSIONS_API,
    phones: PHONES_API
};

// Configuración de formularios por sección
const FORM_CONFIGS = {
    personas: {
        fields: ['dni', 'firstName', 'lastName', 'age', 'sex'],
        createFields: ['dni', 'firstName', 'lastName', 'age', 'sex', 'createDatabase'],
        editFields: ['editDni', 'editFirstName', 'editLastName', 'editAge', 'editSex']
    },
    professions: {
        fields: ['id', 'name', 'description'],
        createFields: ['id', 'name', 'description', 'createDatabase'],
        editFields: ['editId', 'editName', 'editDescription']
    },
    phones: {
        fields: ['number', 'company', 'ownerId'],
        createFields: ['number', 'company', 'ownerId', 'createDatabase'],
        editFields: ['editNumber', 'editCompany', 'editOwnerId']
    }
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
    }
    else if (currentSection === 'phones') {
        displayPhones(items, container);
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

function getGenderText(sex) {
    switch(sex) {
        case 'M': case 'MALE': return 'Masculino';
        case 'F': case 'FEMALE': return 'Femenino';
        case 'O': case 'OTHER': return 'Otro';
        default: return 'No especificado';
    }
}

// Estadísticas
// Actualizar la función updateStats para incluir phones
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
    }
}

// Modificar la función showSection para cargar propietarios cuando se selecciona phones
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
    
    // Si es la sección de teléfonos, cargar propietarios disponibles
    if (section === 'phones') {
        setTimeout(() => loadAvailableOwners(), 500); // Delay para que carguen las personas primero
    }
}

// Actualizar función de inicialización para incluir phones
function initializeApp() {
    setupNavigation();
    showSection('personas');
    loadBothDatabases();
    setupEventListeners();
    
    // Configurar event listener para cuando se cambie a la sección de teléfonos
    document.querySelector('[data-section="phones"]').addEventListener('click', function() {
        setTimeout(() => loadAvailableOwners(), 1000);
    });
}

// Crear elemento
// Función createItem corregida para usar IDs únicos
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
        // Para teléfonos, redirigir a la función específica
        console.log('Redirecting to createPhoneWithValidation for phones');
        createPhoneWithValidation();
        return;
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

// Función específica para crear teléfonos con validación correcta
function createPhoneWithValidation() {
    console.log('=== CREATE PHONE WITH VALIDATION START ===');
    
    // Verificar que estamos en la sección correcta
    if (currentSection !== 'phones') {
        console.error('Not in phones section, current:', currentSection);
        showMessage('❌ Error: No estás en la sección de teléfonos', 'error');
        return;
    }
    
    // Obtener elementos específicos para teléfonos con IDs únicos
    const numberElement = document.getElementById('phoneNumber');
    const companyElement = document.getElementById('phoneCompany');
    const ownerIdElement = document.getElementById('phoneOwnerId');
    const databaseElement = document.getElementById('phoneCreateDatabase');
    
    console.log('=== FORM ELEMENTS CHECK ===');
    console.log('Number element:', numberElement ? 'FOUND' : 'NOT FOUND');
    console.log('Company element:', companyElement ? 'FOUND' : 'NOT FOUND');
    console.log('Owner element:', ownerIdElement ? 'FOUND' : 'NOT FOUND');
    console.log('Database element:', databaseElement ? 'FOUND' : 'NOT FOUND');
    
    if (!numberElement || !companyElement || !ownerIdElement || !databaseElement) {
        showMessage('❌ Error: No se pudieron encontrar todos los elementos del formulario', 'error');
        console.error('Missing elements:', {
            number: !numberElement,
            company: !companyElement,
            owner: !ownerIdElement,
            database: !databaseElement
        });
        return;
    }
    
    // Obtener valores
    const formData = {
        number: numberElement.value.trim(),
        company: companyElement.value.trim(),
        ownerId: ownerIdElement.value.trim(),
        database: databaseElement.value.trim()
    };
    
    console.log('=== FORM DATA ===');
    console.log('Number:', formData.number);
    console.log('Company:', formData.company);
    console.log('Owner ID:', formData.ownerId);
    console.log('Database (raw):', formData.database);
    console.log('Database element selected index:', databaseElement.selectedIndex);
    console.log('Database element options:');
    Array.from(databaseElement.options).forEach((option, index) => {
        console.log(`  [${index}] value: "${option.value}", text: "${option.text}", selected: ${option.selected}`);
    });
    
    // Validaciones
    if (!formData.number) {
        showMessage('❌ Por favor ingrese el número de teléfono', 'error');
        return;
    }
    
    if (!formData.company) {
        showMessage('❌ Por favor ingrese la compañía', 'error');
        return;
    }
    
    if (!formData.ownerId) {
        showMessage('❌ Por favor seleccione un propietario', 'error');
        return;
    }
    
    if (!formData.database) {
        showMessage('❌ Por favor seleccione una base de datos', 'error');
        return;
    }
    
    // Normalizar base de datos
    formData.database = formData.database.toUpperCase();
    
    if (formData.database !== 'MARIA' && formData.database !== 'MONGO') {
        showMessage('❌ Base de datos inválida. Debe ser MARIA o MONGO', 'error');
        return;
    }
    
    console.log('=== VALIDATED FORM DATA ===');
    console.log(JSON.stringify(formData, null, 2));
    
    // Llamar a la función de creación
    createPhoneItem(formData);
}

// Función separada para la creación del teléfono
async function createPhoneItem(formData) {
    console.log('=== CREATE PHONE ITEM START ===');
    
    try {
        setLoading(true);
        
        const requestBody = JSON.stringify(formData);
        console.log('Request body:', requestBody);
        
        const response = await fetch('/api/v1/phone', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: requestBody
        });

        console.log('Response status:', response.status);
        console.log('Response headers:', Object.fromEntries(response.headers.entries()));

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Error response:', errorText);
            throw new Error(`Error ${response.status}: ${errorText}`);
        }

        const result = await response.json();
        console.log('API result:', result);
        
        if (result.status && result.status.startsWith('ERROR')) {
            throw new Error(result.status);
        }

        // Verificar que se creó en la base de datos correcta
        const expectedDB = formData.database === 'MARIA' ? 'MariaDB' : 'MongoDB';
        const actualDB = result.database;
        
        console.log('Expected database:', expectedDB);
        console.log('Actual database:', actualDB);
        
        if (actualDB === expectedDB) {
            showMessage(`✅ Teléfono creado exitosamente en ${actualDB}`, 'success');
        } else {
            showMessage(`⚠️ Teléfono creado en ${actualDB}, pero se esperaba ${expectedDB}`, 'error');
        }
        
        // Limpiar formulario
        document.getElementById('phoneNumber').value = '';
        document.getElementById('phoneCompany').value = '';
        document.getElementById('phoneOwnerId').value = '';
        // No limpiar la base de datos para facilitar múltiples creaciones
        
        // Recargar datos
        loadBothDatabases();
        
    } catch (error) {
        console.error('Create phone error:', error);
        showMessage(`❌ Error al crear teléfono: ${error.message}`, 'error');
    } finally {
        setLoading(false);
    }
}

// Función mejorada de debug para teléfonos
function debugPhoneForm() {
    console.log('=== PHONE FORM DEBUG START ===');
    
    // Buscar elementos específicos para teléfonos
    const elements = {
        phoneNumber: document.getElementById('phoneNumber'),
        phoneCompany: document.getElementById('phoneCompany'),
        phoneOwnerId: document.getElementById('phoneOwnerId'),
        phoneCreateDatabase: document.getElementById('phoneCreateDatabase')
    };
    
    console.log('=== FORM ELEMENTS ===');
    Object.keys(elements).forEach(key => {
        const element = elements[key];
        if (element) {
            console.log(`${key}:`, {
                id: element.id,
                value: element.value,
                selectedIndex: element.selectedIndex || 'N/A',
                options: element.options ? Array.from(element.options).map(o => `${o.value}(${o.selected})`) : 'N/A'
            });
        } else {
            console.log(`${key}: NOT FOUND`);
        }
    });
    
    const databaseElement = elements.phoneCreateDatabase;
    
    console.log('=== DATABASE ELEMENT ANALYSIS ===');
    if (databaseElement) {
        console.log('Using element:', databaseElement.id);
        console.log('Current value:', databaseElement.value);
        console.log('Selected index:', databaseElement.selectedIndex);
        console.log('All options:');
        Array.from(databaseElement.options).forEach((option, index) => {
            console.log(`  [${index}] "${option.value}" - "${option.text}" (selected: ${option.selected})`);
        });
    } else {
        console.error('NO DATABASE ELEMENT FOUND!');
    }
    
    console.log('=== PHONE FORM DEBUG END ===');
    
    return {
        elements,
        databaseElement,
        formValid: !!(elements.phoneNumber?.value && elements.phoneCompany?.value && elements.phoneOwnerId?.value && databaseElement?.value)
    };
}

// Función para forzar la selección de base de datos
function forceSelectDatabase(database) {
    console.log('=== FORCE SELECT DATABASE ===');
    console.log('Target database:', database);
    
    const databaseElement = document.getElementById('phoneCreateDatabase');
    
    if (databaseElement) {
        console.log('Found database element:', databaseElement.id);
        
        // Buscar la opción correcta
        let optionFound = false;
        Array.from(databaseElement.options).forEach((option, index) => {
            if (option.value === database) {
                console.log(`Setting selected index to ${index} (${option.value})`);
                databaseElement.selectedIndex = index;
                option.selected = true;
                optionFound = true;
            } else {
                option.selected = false;
            }
        });
        
        if (optionFound) {
            // Disparar evento change
            databaseElement.dispatchEvent(new Event('change', { bubbles: true }));
            console.log('✅ Database selection forced to:', database);
            console.log('Current value:', databaseElement.value);
        } else {
            console.error('❌ Option not found for database:', database);
        }
    } else {
        console.error('❌ Database element not found');
    }
    
    // Verificar resultado
    setTimeout(() => {
        debugPhoneForm();
    }, 100);
}

// Función para cargar propietarios en el dropdown correcto
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

// Función para debug del formulario de teléfonos
function debugPhoneForm() {
    console.log('=== PHONE FORM DEBUG ===');
    
    const numberField = document.getElementById('number');
    const companyField = document.getElementById('company');
    const ownerField = document.getElementById('ownerId');
    const databaseField = document.getElementById('createDatabase');
    
    console.log('Number field:', numberField ? numberField.value : 'NOT FOUND');
    console.log('Company field:', companyField ? companyField.value : 'NOT FOUND');
    console.log('Owner field:', ownerField ? ownerField.value : 'NOT FOUND');
    console.log('Database field:', databaseField ? databaseField.value : 'NOT FOUND');
    
    if (databaseField) {
        console.log('Database options:');
        for (let option of databaseField.options) {
            console.log('  Option:', option.value, '(selected:', option.selected, ')');
        }
    }
}

function validatePhoneForm() {
    console.log('=== VALIDATING PHONE FORM ===');
    
    debugPhoneForm();
    
    const number = document.getElementById('number').value;
    const company = document.getElementById('company').value;
    const ownerId = document.getElementById('ownerId').value;
    const database = document.getElementById('createDatabase').value;
    
    console.log('Validation results:');
    console.log('  Number valid:', !!number);
    console.log('  Company valid:', !!company);
    console.log('  Owner selected:', !!ownerId);
    console.log('  Database selected:', !!database);
    
    return !!(number && company && ownerId && database);
}

// Modales de edición
function openPersonaEditModal(id, firstName, lastName, age, sex, database) {
    console.log('Opening persona edit modal with:', { id, firstName, lastName, age, sex, database });
    currentEditId = id;
    currentEditDatabase = database === 'MariaDB' ? 'MARIA' : 'MONGO'; // Recordar BD original
    
    // Configurar el modal para personas
    document.getElementById('editModalTitle').textContent = '✏️ Editar Persona';
    document.getElementById('editPersonForm').style.display = 'block';
    document.getElementById('editProfessionForm').style.display = 'none';
    
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
    currentEditDatabase = database === 'MariaDB' ? 'MARIA' : 'MONGO'; // Recordar BD original
    
    // Configurar el modal para profesiones
    document.getElementById('editModalTitle').textContent = '✏️ Editar Profesión';
    document.getElementById('editPersonForm').style.display = 'none';
    document.getElementById('editProfessionForm').style.display = 'block';
    
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
    currentEditId = null;
    currentEditDatabase = null;
}


// Función separada para la creación del teléfono
async function createPhoneItem(formData) {
    console.log('=== CREATE PHONE ITEM START ===');
    
    try {
        setLoading(true);
        
        const requestBody = JSON.stringify(formData);
        console.log('Request body:', requestBody);
        
        const response = await fetch('/api/v1/phone', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: requestBody
        });

        console.log('Response status:', response.status);
        console.log('Response headers:', Object.fromEntries(response.headers.entries()));

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Error response:', errorText);
            throw new Error(`Error ${response.status}: ${errorText}`);
        }

        const result = await response.json();
        console.log('API result:', result);
        
        if (result.status && result.status.startsWith('ERROR')) {
            throw new Error(result.status);
        }

        // Verificar que se creó en la base de datos correcta
        const expectedDB = formData.database === 'MARIA' ? 'MariaDB' : 'MongoDB';
        const actualDB = result.database;
        
        console.log('Expected database:', expectedDB);
        console.log('Actual database:', actualDB);
        
        if (actualDB === expectedDB) {
            showMessage(`✅ Teléfono creado exitosamente en ${actualDB}`, 'success');
        } else {
            showMessage(`⚠️ Teléfono creado en ${actualDB}, pero se esperaba ${expectedDB}`, 'error');
        }
        
        // Limpiar formulario
        document.getElementById('number').value = '';
        document.getElementById('company').value = '';
        document.getElementById('ownerId').value = '';
        // No limpiar la base de datos para facilitar múltiples creaciones
        
        // Recargar datos
        loadBothDatabases();
        
    } catch (error) {
        console.error('Create phone error:', error);
        showMessage(`❌ Error al crear teléfono: ${error.message}`, 'error');
    } finally {
        setLoading(false);
    }
}

// Event listener para detectar cambios en el dropdown
document.addEventListener('DOMContentLoaded', function() {
    // Buscar el elemento cuando la página esté lista
    setTimeout(() => {
        const databaseElement = document.getElementById('phoneCreateDatabase') || document.getElementById('createDatabase');
        
        if (databaseElement) {
            databaseElement.addEventListener('change', function(e) {
                console.log('Database dropdown changed to:', e.target.value);
                console.log('Selected index:', e.target.selectedIndex);
            });
            
            // Establecer valor por defecto
            if (!databaseElement.value) {
                databaseElement.selectedIndex = 0; // Seleccionar primera opción
            }
        }
    }, 1000);
});

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
    }

    try {
        setLoading(true);
        const response = await fetch(`${api}/${currentEditId}`, {
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
    
    const numericId = String(id).trim();
    if (isNaN(numericId) || numericId === '') {
        console.error('ID is not numeric:', numericId);
        showMessage('❌ Error: ID debe ser numérico', 'error');
        return;
    }
    
    console.log('Proceeding with delete - ID:', numericId, 'Database:', database);
    deleteItem(numericId, database);
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
    
    const entityType = currentSection === 'personas' ? 'persona' : 'profesión';
    const message = `¿Está seguro de que desea eliminar la ${entityType} con ID ${currentDeleteId} de ${cleanDatabase}?`;
    document.getElementById('confirmMessage').textContent = message;
    document.getElementById('confirmModal').style.display = 'block';
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

        const entityType = currentSection === 'personas' ? 'Persona' : 'Profesión';
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