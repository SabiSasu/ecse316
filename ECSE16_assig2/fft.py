import os
import re
import sys
import numpy as np
from matplotlib import pyplot as plt
import cv2
from matplotlib.colors import LogNorm


	
def dft(x):
	x = np.asarray(x, dtype=float)
	N = x.shape[0]
	n = np.arange(N)
	k = n.reshape((N, 1))
	M = np.exp(-2j * np.pi * k * n / N)
	return np.dot(M, x)

def FFT(x):
	x = np.asarray(x, dtype=float)
	N = x.shape[0]

	if N % 2 > 0:
		raise ValueError("size of x must be a power of 2")
	elif N <= 2:  # this cutoff should be optimized
		return dft(x)
	else:
		X_even = FFT(x[::2])
		X_odd = FFT(x[1::2])
		factor = np.exp(-2j * np.pi * np.arange(N) / N)
		X_even = np.concatenate([X_even, X_even])
		X_odd = np.concatenate([X_odd, X_odd])
		return X_even + factor * X_odd
		
def mode_1(image):
	im = cv2.imread(image)
	width = im.shape[1]
	height = im.shape[0]
	
	print(width)
	print(height)
	while np.log2(width)%1 != 0:
		width = width+1
	while np.log2(height)%1 != 0:
		height = height+1
	dim = (width, height)
	resized = cv2.resize(im, dim, interpolation = cv2.INTER_AREA) 
	print(resized.shape[1])
	print(resized.shape[0])
	size = len(resized)
	cv2.imshow("Image Resized", resized)
	# construct a grayscale histogram
	x = np.random.random(8)
	im_fft = np.fft.fft(resized)
	im_fft2 = np.fft.fft(x)
	print(resized)
	print("FFT1")
	print(im_fft)
	print(im_fft2)
	fast_dft = FFT(resized)
	
	print(fast_dft)
	# A logarithmic colormap
	plt.figure()
	plt.imshow(np.abs(im_fft), norm=LogNorm(vmin=5))
	plt.colorbar()
	plt.title('Fourier transform')
	plt.show()
	cv2.waitKey(0)

def mode_2(image):
	print(image)
	
def mode_3(image):
	print(image)
	
def mode_4(image):
	print(image)

if __name__ == '__main__':
	n = sys.argv
	size = len(sys.argv)
	if(n[0] != "fft.py"):
		print("Command line synthax must have the form: python fft.py [-m mode] [-i image]")
	print("Input: ", end='')
	print(n)
	
	image = "moonlanding.png"
	mode = 1
	
	modeset = False
	imageset = False
	
	i = 1
	while i < size:
		if n[i] == "-i" and imageset == False:
			image = n[i+1]
			imageset = True
		elif n[i] == "-m" and modeset == False:
			mode = int(n[i+1])
			modeset = True
		else:
			print("Arguments cannot be parsed")
			exit()
		i = i+2
	
	if mode == 1:
		mode_1(image)
	elif mode == 2:
		mode_2(image)
	elif mode == 3:
		mode_3(image)
	elif mode == 4:
		mode_4(image)
	else:
		print("Mode has to be between 1 and 4")
		exit()

	
