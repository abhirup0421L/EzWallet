<svg width="900" height="180" viewBox="0 0 900 180" xmlns="http://www.w3.org/2000/svg">

  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#07111F"/>
      <stop offset="50%" stop-color="#0B1F33"/>
      <stop offset="100%" stop-color="#07111F"/>
    </linearGradient>

```
<linearGradient id="glow" x1="0" y1="0" x2="1" y2="1">
  <stop offset="0%" stop-color="#00E5FF"/>
  <stop offset="100%" stop-color="#7C4DFF"/>
</linearGradient>

<filter id="shadow">
  <feGaussianBlur stdDeviation="5" result="blur"/>
  <feMerge>
    <feMergeNode in="blur"/>
    <feMergeNode in="SourceGraphic"/>
  </feMerge>
</filter>
```

  </defs>

  <!-- Background -->

  <rect width="900" height="180" rx="25" fill="url(#bg)"/>

  <!-- Decorative glowing lines -->

<path d="M0 140 Q150 90 300 140 T600 140 T900 140"
     fill="none"
     stroke="#00E5FF"
     stroke-opacity="0.12"
     stroke-width="2"/>

<path d="M0 40 Q150 90 300 40 T600 40 T900 40"
     fill="none"
     stroke="#7C4DFF"
     stroke-opacity="0.12"
     stroke-width="2"/>

  <!-- LEFT LOCK -->

  <g filter="url(#shadow)">
    <text x="90" y="105" font-size="55">
      🔐
      <animateTransform
        attributeName="transform"
        type="translate"
        values="0 12; 0 -12; 0 12"
        dur="3s"
        repeatCount="indefinite"/>
    </text>
  </g>

  <!-- LEFT SHIELD -->

  <g filter="url(#shadow)">
    <text x="205" y="70" font-size="38">
      🛡️
      <animateTransform
        attributeName="transform"
        type="translate"
        values="0 -8; 0 10; 0 -8"
        dur="3.5s"
        begin="0.5s"
        repeatCount="indefinite"/>
    </text>
  </g>

  <!-- RIGHT LOCK -->

  <g filter="url(#shadow)">
    <text x="755" y="105" font-size="55">
      🔒
      <animateTransform
        attributeName="transform"
        type="translate"
        values="0 -10; 0 12; 0 -10"
        dur="3.2s"
        begin="0.8s"
        repeatCount="indefinite"/>
    </text>
  </g>

  <!-- RIGHT SHIELD -->

  <g filter="url(#shadow)">
    <text x="650" y="72" font-size="38">
      🛡️
      <animateTransform
        attributeName="transform"
        type="translate"
        values="0 10; 0 -8; 0 10"
        dur="3.8s"
        begin="1s"
        repeatCount="indefinite"/>
    </text>
  </g>

  <!-- SMALL FLOATING LOCK -->

  <text x="330" y="45" font-size="25" opacity="0.8">
    🔒
    <animateTransform
      attributeName="transform"
      type="translate"
      values="0 8; 0 -8; 0 8"
      dur="2.7s"
      begin="0.3s"
      repeatCount="indefinite"/>
  </text>

  <!-- SMALL FLOATING SHIELD -->

  <text x="555" y="145" font-size="25" opacity="0.8">
    🛡️
    <animateTransform
      attributeName="transform"
      type="translate"
      values="0 -8; 0 8; 0 -8"
      dur="3s"
      begin="1.2s"
      repeatCount="indefinite"/>
  </text>

  <!-- CENTER GLOW -->

<circle cx="450" cy="90" r="58"
       fill="none"
       stroke="url(#glow)"
       stroke-width="2"
       opacity="0.35"> <animate
   attributeName="r"
   values="48;65;48"
   dur="3s"
   repeatCount="indefinite"/> <animate
   attributeName="opacity"
   values="0.2;0.5;0.2"
   dur="3s"
   repeatCount="indefinite"/> </circle>

  <!-- TITLE -->

<text x="450"
     y="82"
     text-anchor="middle"
     fill="white"
     font-family="Arial, sans-serif"
     font-size="30"
     font-weight="bold">
💰 EzWallet </text>

  <!-- SUBTITLE -->

<text x="450"
     y="112"
     text-anchor="middle"
     fill="#00E5FF"
     font-family="Arial, sans-serif"
     font-size="15">
SECURE • ORGANIZED • PERSONALIZED </text>

</svg>
