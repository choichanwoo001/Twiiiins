<template>
  <div class="shop">
    <aside class="shop-title"><h1>SHOP</h1></aside>
    <main class="shop-content" :lang="websiteLanguage">
      <article class="album-product" aria-labelledby="album-title">
        <figure class="album-artwork">
          <img :src="albumCover" alt="TWIIIINS – Only The Beginning album cover, with flowing orange, black and cream patterns"
            width="1600" height="1600" decoding="async">
        </figure>
        <div class="album-details">
          <p class="album-format">TWIIIINS · CD</p>
          <h2 id="album-title">Only The Beginning</h2>
          <p class="album-description">{{ copy.description }}</p>
          <p class="album-price">€13</p>
          <p class="album-shipping">{{ copy.shipping }}</p>
          <div class="album-order">
            <p>{{ copy.order }}</p>
            <a class="order-button" :href="orderEmail">{{ copy.orderButton }} <span aria-hidden="true">↗</span></a>
            <a class="order-email" href="mailto:stickypeanutbutter@proton.me">stickypeanutbutter@proton.me</a>
          </div>
        </div>
      </article>
    </main>
  </div>
</template>

<script setup>
import albumCover from '../imgs/shop/only-the-beginning.webp'
import { websiteLanguage, useWebsiteCopy } from '../composables/useWebsitePreferences'

const orderEmail = `mailto:stickypeanutbutter@proton.me?subject=${encodeURIComponent('TWIIIINS – Only The Beginning CD order')}`
const copy = useWebsiteCopy({
  description: 'Get your physical copy of TWIIIINS – Only The Beginning.',
  shipping: 'Shipping is not included in the price and is calculated separately depending on the destination.',
  order: 'To order a CD, please contact us directly via email. We’ll get back to you with payment and shipping details.',
  orderButton: 'Order by email'
}, {
  description: 'Hol dir dein physisches Exemplar von TWIIIINS – Only The Beginning.',
  shipping: 'Versandkosten sind nicht im Preis enthalten und werden je nach Zielort separat berechnet.',
  order: 'Um eine CD zu bestellen, kontaktiere uns bitte direkt per E-Mail. Wir melden uns mit den Zahlungs- und Versanddetails bei dir.',
  orderButton: 'Per E-Mail bestellen'
})
</script>

<style scoped>
.shop {
  background: #fff;
  color: #222;
  padding: 3rem 4rem 0 2rem;
  display: grid;
  grid-template-columns: 1fr 2.2fr;
  align-items: start;
  gap: 4rem;
  height: calc(100dvh - var(--navbar-height) - 4rem);
  box-sizing: border-box;
  overflow: hidden;
}
.shop-title { padding-top: 2rem; }
.shop-title h1 {
  font-size: clamp(2.5rem, 6vw, 4.5rem);
  font-weight: 400;
  letter-spacing: 0.12em;
  color: #C1E1DC;
  line-height: 1;
  margin: 0;
}
.shop-content {
  min-width: 0;
  min-height: 0;
  max-width: 62.5rem;
  width: 100%;
  height: 100%;
  justify-self: end;
  padding: 1rem 0 4rem;
  box-sizing: border-box;
  overflow-y: auto;
  scrollbar-gutter: stable;
}
.album-product {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 1fr);
  align-items: start;
  gap: 2rem;
  margin-top: 1.5rem;
}
.album-artwork { margin: 0; }
.album-artwork img { display: block; width: 100%; height: auto; aspect-ratio: 1; }
.album-details { min-width: 0; }
.album-format { margin: 0 0 0.75rem; color: #777; font-size: 0.8rem; letter-spacing: 0.1em; }
.album-details h2 { margin: 0 0 1.25rem; font-size: clamp(1.75rem, 2.5vw, 2.5rem); line-height: 1.15; font-weight: 400; }
.album-description, .album-shipping, .album-order p { color: #555; font-size: 0.95rem; line-height: 1.7; margin: 0; }
.album-price { margin: 1.75rem 0 0.5rem; font-size: 1.6rem; font-weight: 400; }
.album-shipping { font-size: 0.85rem; }
.album-order { border-top: 1px solid #e8e3dd; margin-top: 1.5rem; padding-top: 1.5rem; }
.order-button {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-top: 1.25rem;
  min-height: 2.75rem;
  padding: 0.65rem 1rem;
  box-sizing: border-box;
  border: 1px solid #222;
  color: #222;
  text-decoration: none;
  font-size: 0.95rem;
  transition: background-color 0.15s ease, color 0.15s ease;
}
.order-button:hover { background: #222; color: #fff; }
.order-email { display: inline-block; margin-top: 0.75rem; font-size: 0.8rem; color: #555; overflow-wrap: anywhere; text-underline-offset: 0.2em; }
.order-button:focus-visible, .order-email:focus-visible { outline: 2px solid #8b4513; outline-offset: 4px; }
@media (max-width: 1100px) {
  .album-product { grid-template-columns: minmax(0, 1fr); }
  .album-artwork, .album-details { max-width: 28rem; }
}
@media (max-width: 768px) {
  .shop {
    display: flex;
    flex-direction: column;
    padding: 4rem 1.5rem 6rem;
    gap: 1.5rem;
    height: auto;
    min-height: calc(100dvh - var(--navbar-height));
    overflow: visible;
  }
  .shop-title { padding-top: 0; }
  .shop-content { padding: 0; height: auto; max-width: 100%; overflow: visible; }
  .album-product { margin-top: 1rem; gap: 1.5rem; }
  .album-artwork, .album-details { max-width: 100%; }
}
</style>
