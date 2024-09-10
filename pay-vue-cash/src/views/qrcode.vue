<template>
  <div>
    <canvas ref="qrcodeCanvas" :width="size" :height="size"></canvas>
  </div>
</template>

<script>
import QRCode from 'qrcode';

export default {
  data() {
    return {
      qrText: 'https://qr.alipay.com/_d?_b=PAI_LOGIN_DY&securityId=web%257Cauthcenter_qrcode_login%257Ca01544ac-0fe6-409d-8193-b3acec615768RZ42',
      logoUrl: 'https://img.alicdn.com/tfs/TB1qEwuzrj1gK0jSZFOXXc7GpXa-32-32.ico',
      size: 300, // QR code size
      logoSize: 50 // Logo size
    };
  },
  mounted() {
    this.generateQRCodeWithLogo();
  },
  methods: {
    async generateQRCodeWithLogo() {
      try {
        // Generate QR code to canvas
        const canvas = this.$refs.qrcodeCanvas;
        const ctx = canvas.getContext('2d');
        await QRCode.toCanvas(canvas, this.qrText, {
          errorCorrectionLevel: 'H',
          width: this.size,
        });

        // Load logo image
        const logoImage = new Image();
        logoImage.src = this.logoUrl;
        logoImage.onload = () => {
          // Calculate logo position
          const x = (this.size / 2) - (this.logoSize / 2);
          const y = (this.size / 2) - (this.logoSize / 2);

          // Draw logo on top of QR code
          ctx.drawImage(logoImage, x, y, this.logoSize, this.logoSize);
        };
      } catch (error) {
        console.error('Error generating QR code with logo:', error);
      }
    },
  },
};
</script>

<style scoped>
/* Add any necessary styles here */
</style>
