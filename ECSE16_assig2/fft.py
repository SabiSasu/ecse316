import os
import re
import sys
import numpy as np
import matplotlib
import plotly
import cv2

def mode_1(image):
	print(image)

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

	
