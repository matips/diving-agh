# Diving AGH 2015
Prezentacja: http://matips.github.io/diving-agh   

Wersja do czytania: https://github.com/matips/diving-agh/blob/gh-pages/index.md
---
# Decompresion
Decompression theory is the study and modelling of the transfer of the inert gas component of breathing gases from the gas in the lungs to the tissues and back during exposure to variations in ambient pressure.

# Modeled effects
Decompression involves a complex interaction of gas solubility, partial pressures and concentration gradients, diffusion, bulk transport and bubble mechanics in living tissues.
---
# Solubility of gases at 37 °C[1]
<table class="rich-diff-level-zero"> <thead class="rich-diff-level-one"> <tr> <th>Gas Molecular</th> <th>weight</th> <th>Water solubility</th> <th>Lipid solubility</th> <th>Water/lipid solubility ratio</th> </tr> </thead> <tbody class="rich-diff-level-one"> <tr> <td>Hydrogen</td> <td>2</td> <td>0.016</td> <td>0.048</td> <td>3.1</td> </tr> <tr> <td>Helium</td> <td>4</td> <td>0.0085</td> <td>0.015</td> <td>1.7</td> </tr> <tr> <td>Neon</td> <td>20</td> <td>0.0097</td> <td>0.019</td> <td>2.07</td> </tr> <tr> <td>Nitrogen</td> <td>28</td> <td>0.013</td> <td>0.067</td> <td>5.2</td> </tr> <tr> <td>Oxygen</td> <td>32</td> <td>0.024</td> <td>0.12</td> <td>5.0</td> </tr> <tr> <td>Carbon dioxide</td> <td>44</td> <td>0.56</td> <td>0.876</td> <td>1.6</td> </tr> </tbody> </table>
---
# Inert gas uptake (Ingassing)
In this context, inert gas refers to a gas which is not metabolically active. Atmospheric nitrogen (N2) is the most common example, and helium (He) is the other inert gas commonly used in breathing mixtures for divers.   
At atmospheric pressure the body tissues are therefore normally saturated with nitrogen at 0.758bar (569mmHg). At increased ambient pressures due to depth or habitat pressurisation, a diver's lungs are filled with breathing gas at the increased pressure, and the partial pressures of the constituent gases will be increased proportionately.
---
# Tissue half times
Half time of a tissue is the time it takes for the tissue to take up or release 50% of the difference in dissolved gas capacity at a changed partial pressure. 
---
# Decompression models
Actual rates of diffusion and perfusion, and solubility of gases in specific tissues is not generally known, and vary considerably. However mathematical models have been proposed that approximate the real situation to a greater or lesser extent. These models predict whether symptomatic bubble formation is likely to occur for a given dive profile. Algorithms based on these models produce decompression tables. In personal dive computers, they produce a real-time estimate of decompression status and display it for the diver.
---
# Saturation 
If the supply of gas to a solvent is unlimited, the gas will diffuse into the solvent until there is so much dissolved that equilibrium is reached and the amount diffusing back out is equal to the amount diffusing in. This is called saturation.
   
<img src="http://upload.wikimedia.org/wikipedia/commons/thumb/5/5b/Tissue_half_times_%281%29.svg/360px-Tissue_half_times_%281%29.svg.png" />
---
# Critical ratio model
J.S. Haldane originally used a pressure ratio of 2 to 1 for decompression on the principle that the saturation of the body should at no time be allowed to exceed about double the air pressure. This principle was applied as a pressure ratio of total ambient pressure and did not take into account the partial pressures of the component gases of the breathing air. His experimental work on goats and observations of human divers appeared to support this assumption. However, in time, this was found to be inconsistent with incidence of decompression sickness and changes were made to the initial assumptions. This was later changed to a 1.58:1 ratio of nitrogen partial pressures.

# Critical difference models
Further research by people such as Robert Workman suggested that the criterion was not the ratio of pressures, but the actual pressure differentials. Applied to Haldane's work, this would suggest that the limit is not determined by the 1.58:1 ratio but rather by the difference of 0.58 atmospheres between tissue pressure and ambient pressure. Most tables today, including the Bühlmann tables, are based on the critical difference model.
---
# M-values
At a given ambient pressure, the M-value is the maximum value of absolute inert gas pressure that a tissue compartment can take without presenting symptoms of decompression sickness. M-values are limits for the tolerated gradient between inert gas pressure and ambient pressure in each compartment. Alternative terminology for M-values include "supersaturation limits", "limits for tolerated overpressure", and "critical tensions".
---
# Bubble mechanics
Equilibrium of forces on the surface is required for a bubble to exist. These are:

- Ambient pressure, exerted on the outside of the surface, acting inwards
- Pressure due to tissue distortion, also on the outside and acting inwards
- Surface tension of the liquid at the interface between the bubble and the surroundings. This is along the surface of the bubble, so the resultant acts towards the centre of curvature. This will tend to squeeze the bubble, and is more severe for small bubbles as it is an inverse function of the radius.
- The resulting forces must be balanced by the pressure on the inside of the bubble. This is the sum of the partial pressures of the gases inside due to the net diffusion of gas to and from the bubble.
- The force balance in the bubble may be modified by a layer of surface active molecules which can stabilise a microbubble at a size where surface tension on a clean bubble would cause it to collapse rapidly.
- This surface layer may vary in permeability, so that if the bubble is compressed it may become impermeable to diffusion at sufficient compression.
---
If the solvent outside the bubble is saturated or unsaturated, the partial pressure will be less than in the bubble, and the surface tension will be increasing the internal pressure in direct proportion to surface curvature, providing a pressure gradient to increase diffusion out of the bubble, effectively "squeezing the gas out of the bubble", and the smaller the bubble the faster it will get squeezed out. A gas bubble can only grow at constant pressure if the surrounding solvent is sufficiently supersaturated to overcome the surface tension or if the surface layer provides sufficient reaction to overcome surface tension.
---
# Bubble models
Bubble decompression models are a rule based approach to calculating decompression based on the idea that microscopic bubble nuclei always exist in water and tissues that contain water and that by predicting and controlling the bubble growth, one can avoid decompression sickness. Most of the bubble models assume that bubbles will form during decompression, and that mixed phase gas elimination occurs.

Decompression models that assume mixed phase gas elimination include:

- The arterial bubble decompression model of the French Tables du Ministère du Travail 1992
- The U.S.Navy Exponential-Linear (Thalmann) algorithm used for the 2008 US Navy air decompression tables (among others)
- Hennessy's combined perfusion/diffusion model of the BSAC'88 tables
- The Varying Permeability Model (VPM) developed by D.E. Yount and others at the University of Hawaii
- The Reduced Gradient Bubble Model (RGBM) developed by Bruce Wienke at Los Alamos National Laboratory
