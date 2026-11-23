const enlacePublico = document.querySelector('#enlace-publico');

if (enlacePublico) {
    const url = encodeURIComponent(enlacePublico.href);
    const facebook = document.querySelector('[data-red="facebook"]');
    const whatsapp = document.querySelector('[data-red="whatsapp"]');
    facebook.href = 'https://www.facebook.com/sharer/sharer.php?u=' + url;
    whatsapp.href = 'https://wa.me/?text=' + url;
    facebook.hidden = false;
    whatsapp.hidden = false;
}
