const API_BASE = 'http://localhost:8080/api';
let activeCustomerId = null;

async function fetchJson(url, options = {}) {
    const response = await fetch(url, options);
    if (!response.ok) {
        const error = await response.json().catch(() => ({ error: response.statusText }));
        throw new Error(error.error || 'Request failed');
    }
    if (response.status === 204) {
        return null;
    }
    return response.json();
}

async function loadCustomers() {
    let customers = [];
    try {
        customers = await fetchJson(`${API_BASE}/customers`);
        if (!Array.isArray(customers)) {
            customers = [];
        }
    } catch (error) {
        console.warn('Falling back to static customers because the API is unavailable.', error);
    }

    const fallbackCustomers = [
        { id: 'alex', name: 'Alex Johnson' },
        { id: 'taylor', name: 'Taylor Smith' }
    ];

    const options = customers.length ? customers : fallbackCustomers;
    const container = document.getElementById('customer-selector');
    container.innerHTML = '';
    const select = document.createElement('select');
    options.forEach(customer => {
        const option = document.createElement('option');
        option.value = customer.id;
        option.textContent = customer.name;
        select.appendChild(option);
    });

    if (!options.length) {
        const option = document.createElement('option');
        option.textContent = 'No customers available';
        option.disabled = true;
        select.appendChild(option);
        select.disabled = true;
    }

    select.addEventListener('change', event => {
        activeCustomerId = event.target.value;
        refreshCart();
        loadOrders();
    });
    container.appendChild(select);
    activeCustomerId = select.value;
}

async function loadProducts() {
    try {
        const products = await fetchJson(`${API_BASE}/products`) || [];
        const list = document.getElementById('product-list');
        list.innerHTML = '';
        products.forEach(product => {
            const card = document.createElement('div');
            card.className = 'product-card';
            card.innerHTML = `
                <h3>${product.name}</h3>
                <p>${product.description || ''}</p>
                <div class="price">$${product.price}</div>
                <div class="stock">In stock: ${product.stock}</div>
            `;
            const button = document.createElement('button');
            button.textContent = 'Add to cart';
            button.addEventListener('click', () => addToCart(product.id));
            card.appendChild(button);
            list.appendChild(card);
        });
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function refreshCart() {
    if (!activeCustomerId) return;
    try {
        const cart = await fetchJson(`${API_BASE}/carts/${activeCustomerId}`) || { items: [] };
        renderCart(cart);
    } catch (error) {
        showMessage(error.message, true);
    }
}

function renderCart(cart) {
    const container = document.getElementById('cart-items');
    container.innerHTML = '';
    if (!cart.items || !cart.items.length) {
        container.innerHTML = '<p>Your cart is empty.</p>';
    }
    (cart.items || []).forEach(item => {
        const element = document.createElement('div');
        element.className = 'cart-item';
        element.innerHTML = `
            <span>${item.productName}</span>
            <input type="number" min="1" value="${item.quantity}" />
            <span>$${item.priceSnapshot}</span>
        `;
        const input = element.querySelector('input');
        input.addEventListener('change', (event) => updateQuantity(item.productId, parseInt(event.target.value, 10)));
        const removeButton = document.createElement('button');
        removeButton.textContent = 'Remove';
        removeButton.addEventListener('click', () => removeItem(item.productId));
        element.appendChild(removeButton);
        container.appendChild(element);
    });
    document.getElementById('cart-total').textContent = cart.items
        .reduce((total, item) => total + item.quantity * parseFloat(item.priceSnapshot), 0)
        .toFixed(2);
}

async function addToCart(productId) {
    try {
        await fetchJson(`${API_BASE}/carts/${activeCustomerId}/items`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId, quantity: 1 })
        });
        refreshCart();
        showMessage('Item added to cart');
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function updateQuantity(productId, quantity) {
    try {
        await fetchJson(`${API_BASE}/carts/${activeCustomerId}/items/${productId}`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ quantity })
        });
        refreshCart();
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function removeItem(productId) {
    try {
        await fetchJson(`${API_BASE}/carts/${activeCustomerId}/items/${productId}`, {
            method: 'DELETE'
        });
        refreshCart();
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function checkout() {
    try {
        await fetchJson(`${API_BASE}/orders/checkout`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ customerId: activeCustomerId })
        });
        await refreshCart();
        await loadOrders();
        showMessage('Order placed successfully!');
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function loadOrders() {
    if (!activeCustomerId) return;
    try {
        const orders = await fetchJson(`${API_BASE}/orders/customers/${activeCustomerId}`) || [];
        const container = document.getElementById('orders');
        container.innerHTML = '';
        if (!orders.length) {
            container.innerHTML = '<p>No orders yet. Complete a checkout to see your order history.</p>';
        }
        orders.forEach(order => {
            const card = document.createElement('div');
            card.className = 'order-card';
            card.innerHTML = `
                <h3>Order ${order.id}</h3>
                <p>${new Date(order.createdAt).toLocaleString()}</p>
                <p>Total: $${order.total}</p>
            `;
            const list = document.createElement('ul');
            order.items.forEach(item => {
                const li = document.createElement('li');
                li.textContent = `${item.productName} x${item.quantity}`;
                list.appendChild(li);
            });
            card.appendChild(list);
            container.appendChild(card);
        });
    } catch (error) {
        showMessage(error.message, true);
    }
}

function showMessage(message, isError = false) {
    const container = document.getElementById('message');
    container.textContent = message;
    container.style.color = isError ? '#e11d48' : '#2563eb';
}

document.getElementById('checkout-button').addEventListener('click', checkout);

(async function init() {
    await loadCustomers();
    await loadProducts();
    await refreshCart();
    await loadOrders();
})();
