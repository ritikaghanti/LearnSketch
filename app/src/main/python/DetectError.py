from skimage.metrics import structural_similarity 
import cv2
import numpy as np
from os.path import dirname, join
import os
import io
import base64
from PIL import Image

def detectError(refdata, sketchdata):
    try:
            
            ref_decodedata = base64.b64decode(refdata)
            sketch_decodedata = base64.b64decode(sketchdata)
            np_ref = np.fromstring(ref_decodedata,np.uint8)
            np_sketch = np.fromstring(sketch_decodedata,np.uint8)
            reference_img = cv2.imdecode(np_ref, cv2.IMREAD_UNCHANGED)
            first_gray = Canny(reference_img)
            print(reference_img.shape)
            print(first_gray.shape)
            second_img = cv2.imdecode(np_sketch, cv2.IMREAD_UNCHANGED)
            # reference = "./reference.jpg"
        #     first_gray = 255 - cv2.cvtColor(first, cv2.COLOR_BGR2GRAY)

            second_gray = 255 - cv2.cvtColor(second_img, cv2.COLOR_BGR2GRAY)
            output_path = join(dirname(__file__), "cannyoutput.png")
            cv2.imwrite(output_path, second_gray)
            print("Canny Image saved successfully!")
            score, diff = structural_similarity(first_gray, second_gray, full=True)
           
            # print("SSIM SCORE: {:.3f}%".format(score * 100))
            diff = (diff * 255).astype("uint8")
            _, thresh = cv2.threshold(diff, 0, 255, cv2.THRESH_BINARY_INV | cv2.THRESH_OTSU)
            contours, _ = cv2.findContours(thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
            mask = np.zeros(first_gray.shape, dtype='uint8')
            cv2.drawContours(mask, contours, -1, (0, 0, 0), -1)
            ssim_output_path = join(dirname(__file__), "ssimoutput.png")
            ssim_output = cv2.drawContours(second_img.copy(), contours, -1, (0, 0, 255), thickness=2)
            ssim_output = cv2.cvtColor(ssim_output, cv2.COLOR_BGR2RGB)
            cv2.imwrite(ssim_output_path, ssim_output)

            
            # img_encode = cv2.imencode('.png', ssim_output).tobytes()
            # ssim_output_string = base64.b64decode(img_encode)

            pilimg = Image.fromarray(ssim_output)
            buff = io.BytesIO()
            pilimg.save(buff,format="PNG")
            # img_str = base64.b64encode(buff.getvalue())
            img_str = base64.b64encode(buff.getvalue()).decode('utf-8')
            final_score = "{:.3f}%".format(score*100)

            # return bytearrayOutput
            # return ""+str(img_str,'utf-8')
            return img_str, final_score
        
    except Exception as e:
            print("An error occurred:", str(e))
            return None
    

def Canny(image):
       
        #Read Input Reference Image

        # kernel for dilation
        kernel = np.ones((3,3),np.uint8)

        ### Passing reference image to CannyAlgo
        edges = cv2.Canny(image, 100, 200, 3, L2gradient=True)

        # ## Dilating output image from canny (Thickness)
        edges = cv2.dilate(edges, kernel, iterations=1)
        smoothed_image = cv2.GaussianBlur(edges, (3, 3), 0)
        return smoothed_image