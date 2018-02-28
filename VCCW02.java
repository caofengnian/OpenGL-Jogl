import static com.jogamp.opengl.GL3.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.swing.JFrame;

import Basic.ShaderProg;
import Basic.Transform;
import Basic.Vec4;
import Objects.SCylinder;
import Objects.SObject;
import Objects.SSphere;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

public class VCCW02 extends JFrame{

	final GLCanvas canvas; //Define a canvas 
	final FPSAnimator animator=new FPSAnimator(60, true);
	final Renderer renderer = new Renderer();

	public VCCW02() {
        GLProfile glp = GLProfile.get(GLProfile.GL3);
        GLCapabilities caps = new GLCapabilities(glp);
        canvas = new GLCanvas(caps);

		add(canvas, java.awt.BorderLayout.CENTER); // Put the canvas in the frame
		canvas.addGLEventListener(renderer); //Set the canvas to listen GLEvents
		
		animator.add(canvas);

		setTitle("Coursework 2");
		setSize(500,500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		animator.start();
		canvas.requestFocus();
		}

	public static void main(String[] args) {
		new VCCW02();

	}

	class Renderer implements GLEventListener {

		private Transform T = new Transform(); //model_view transform

		//VAOs and VBOs parameters
		private int idPoint=0, numVAOs = 2;
		private int idBuffer=0, numVBOs = 2;
		private int idElement=0, numEBOs = 2;
		private int[] VAOs = new int[numVAOs];
		private int[] VBOs = new int[numVBOs];
		private int[] EBOs = new int[numEBOs];

		//Model parameters
		private int[] numElements = new int[numEBOs];
		private long vertexSize; 
		private long normalSize; 
		private int vPosition;
		private int vNormal;

		//Transformation parameters
		private int ModelView;
		private int NormalTransform;
		private int Projection; 

		//Lighting parameter
		private int AmbientProduct;
		private int DiffuseProduct;
		private int SpecularProduct;			
		private int Shininess;

		private float[] ambient1; 
	    private float[] diffuse1;
	    private float[] specular1;
	    private float  materialShininess1;
	    
		private float[] ambient2; 
	    private float[] diffuse2;
	    private float[] specular2;
	    private float  materialShininess2;

		@Override
		public void display(GLAutoDrawable drawable) {
			GL3 gl = drawable.getGL().getGL3(); // Get the GL pipeline object this 
			
			gl.glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);

			//Transformation for the first object (a sphere)
			T.initialize();
			T.scale(0.2f, 0.2f, 0.2f);
			T.rotateX(-90);
			T.translate(0, 0.4f, 0);
		    //Add code here to transform the first object (the sphere) into the position
		    //as suggested in the coursework description, and draw it

			//Locate camera
//			T.LookAt(0, 0, 0, 0, 0, -100, 0, 1, 0);  	//Default					
			
			//Send model_view and normal transformation matrices to shader. 
			//Here parameter 'true' for transpose means to convert the row-major  
			//matrix to column major one, which is required when vertices'
			//location vectors are pre-multiplied by the model_view matrix.
			//Note that the normal transformation matrix is the inverse-transpose
			//matrix of the vertex transformation matrix
			gl.glUniformMatrix4fv( ModelView, 1, true, T.getTransformv(), 0 );			
			gl.glUniformMatrix4fv( NormalTransform, 1, true, T.getInvTransformTv(), 0 );			
			
			//send other uniform variables to shader
			gl.glUniform4fv( AmbientProduct, 1, ambient1,0 );
		    gl.glUniform4fv( DiffuseProduct, 1, diffuse1, 0 );
		    gl.glUniform4fv( SpecularProduct, 1, specular1, 0 );			
		    gl.glUniform1f( Shininess, materialShininess1);

			idPoint=0;
			idBuffer=0;
			idElement=0;
			bindObject(gl);
		    gl.glDrawElements(GL_TRIANGLES, numElements[idElement], GL_UNSIGNED_INT, 0);	
		   
		    //Add code here to transform the second object (a cylinder) into the position
		    //as suggested in the coursework description, and draw it
		    T.initialize();
		    T.scale(0.2f, 0.2f, 0.2f);
		    T.rotateX(-90);
		    gl.glUniformMatrix4fv( ModelView, 1, true, T.getTransformv(), 0 );			
			gl.glUniformMatrix4fv( NormalTransform, 1, true, T.getInvTransformTv(), 0 );	
		   
			gl.glUniform4fv( AmbientProduct, 1, ambient2,0 );
		    gl.glUniform4fv( DiffuseProduct, 1, diffuse2, 0 );
		    gl.glUniform4fv( SpecularProduct, 1, specular2, 0 );			
		    gl.glUniform1f( Shininess, materialShininess2);
		    
			idPoint=1;
			idBuffer=1;
			idElement=1;
			bindObject(gl);
		    gl.glDrawElements(GL_TRIANGLES, numElements[idElement], GL_UNSIGNED_INT, 0);	

		}

		
		@Override
		public void init(GLAutoDrawable drawable) {
			GL3 gl = drawable.getGL().getGL3(); // Get the GL pipeline object this 

			System.out.print("GL_Version: " + gl.glGetString(GL_VERSION));
			
			gl.glEnable(GL_CULL_FACE); 

			//compile and use the shader program
			ShaderProg shaderproc = new ShaderProg(gl, "Gouraud.vert", "Gouraud.frag");
			int program = shaderproc.getProgram();
			gl.glUseProgram(program);

			// Initialize the vertex position and normal attribute in the vertex shader    
		    vPosition = gl.glGetAttribLocation( program, "vPosition" );
		    vNormal = gl.glGetAttribLocation( program, "vNormal" );

		    // Get connected with the ModelView, NormalTransform, and Projection matrices
		    // in the vertex shader
		    ModelView = gl.glGetUniformLocation(program, "ModelView");
		    NormalTransform = gl.glGetUniformLocation(program, "NormalTransform");
		    Projection = gl.glGetUniformLocation(program, "Projection");

		    // Get connected with uniform variables AmbientProduct, DiffuseProduct, 
		    // SpecularProduct, and Shininess in the vertex shader		    
		    AmbientProduct = gl.glGetUniformLocation(program, "AmbientProduct");
		    DiffuseProduct = gl.glGetUniformLocation(program, "DiffuseProduct");
		    SpecularProduct = gl.glGetUniformLocation(program, "SpecularProduct");			
		    Shininess = gl.glGetUniformLocation(program, "Shininess");
			
		    // Generate VAOs, VBOs, and EBOs
		    gl.glGenVertexArrays(numVAOs,VAOs,0);
			gl.glGenBuffers(numVBOs, VBOs,0);
			gl.glGenBuffers(numEBOs, EBOs,0);

		    // Initialize shader lighting parameters
		    float[] lightPosition = {10.0f, 15.0f, 20.0f, 1.0f};
		    Vec4 lightAmbient = new Vec4(0.7f, 0.7f, 0.7f, 1.0f);
		    Vec4 lightDiffuse = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
		    Vec4 lightSpecular = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);

		    gl.glUniform4fv( gl.glGetUniformLocation(program, "LightPosition"),
				  1, lightPosition, 0 );
			//create the first object: a sphere
		    SObject sphere = new SSphere(1,40,40);
			idPoint=0;
			idBuffer=0;
			idElement=0;
			createObject(gl, sphere);

			// Set Sphere material
		    Vec4 materialAmbient1 = new Vec4(0.5f, 0.0f, 0.0f, 1.0f);	
		    Vec4 materialDiffuse1 = new Vec4(0.7f, 0.0f, 0.0f, 1.0f);
		    Vec4 materialSpecular1 = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
		    materialShininess1 = 64.0f;

		    Vec4 ambientProduct = lightAmbient.times(materialAmbient1);
		    ambient1 = ambientProduct.getVector();
		    Vec4 diffuseProduct = lightDiffuse.times(materialDiffuse1);
		    diffuse1 = diffuseProduct.getVector();
		    Vec4 specularProduct = lightSpecular.times(materialSpecular1);
		    specular1 = specularProduct.getVector();

			//Create the second object (a cylinder) here, and set the  
		    //cylinder material different to the sphere

		    
		    SObject scylinder = new SCylinder(1,40,40);
			idPoint=1;
			idBuffer=1;
			idElement=1;
			createObject(gl, scylinder);

			// Set SCylinder material
			Vec4 lightAmbient2 = new Vec4(1.5f, 1.5f, 1.5f, 1.0f);	
		    Vec4 lightDiffuse2 = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
		    Vec4 lightSpecular2 = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
		    //set cylinder light
		    
			Vec4 materialAmbient2 = new Vec4(0.0215f, 0.1745f, 0.0215f, 0.55f);	
		    Vec4 materialDiffuse2 = new Vec4(0.07568f, 0.61424f, 0.07568f, 0.55f);
		    Vec4 materialSpecular2 = new Vec4(0.633f, 0.727811f, 0.633f, 0.55f);
		    materialShininess2 = 12.8f;
		    //set cylinder material

		    Vec4 ambientProduct2 = lightAmbient2.times(materialAmbient2);
		    ambient2 = ambientProduct2.getVector();
		    Vec4 diffuseProduct2 = lightDiffuse2.times(materialDiffuse2);
		    diffuse2 = diffuseProduct2.getVector();
		    Vec4 specularProduct2 = lightSpecular2.times(materialSpecular2);
		    specular2 = specularProduct2.getVector();
		    // This is necessary. Otherwise, the The color on back face may display 
//		    gl.glDepthFunc(GL_LESS);
		    gl.glEnable(GL_DEPTH_TEST);		    
		}
		
		@Override
		public void reshape(GLAutoDrawable drawable, int x, int y, int w,
				int h) {

			GL3 gl = drawable.getGL().getGL3(); // Get the GL pipeline object this 
			
			gl.glViewport(x, y, w, h);

			T.initialize();

			//projection
			if(h<1){h=1;}
			if(w<1){w=1;}			
			float a = (float) w/ h;   //aspect 
			if (w < h) {
				T.ortho(-1, 1, -1/a, 1/a, -1, 1);
			}
			else{
				T.ortho(-1*a, 1*a, -1, 1, -1, 1);
			}
			
			// Convert right-hand to left-hand coordinate system
			T.reverseZ();
		    gl.glUniformMatrix4fv( Projection, 1, true, T.getTransformv(), 0 );			

		}
		
		@Override
		public void dispose(GLAutoDrawable drawable) {
			// TODO Auto-generated method stub
			
		}
		
		public void createObject(GL3 gl, SObject obj) {
			float [] vertexArray = obj.getVertices();
			float [] normalArray = obj.getNormals();
			int [] vertexIndexs =obj.getIndices();
			numElements[idElement] = obj.getNumIndices();

			bindObject(gl);
			
			FloatBuffer vertices = FloatBuffer.wrap(vertexArray);
			FloatBuffer normals = FloatBuffer.wrap(normalArray);
			
		    // Create an empty buffer with the size we need 
			// and a null pointer for the data values
			vertexSize = vertexArray.length*(Float.SIZE/8);
			normalSize = normalArray.length*(Float.SIZE/8);
			gl.glBufferData(GL_ARRAY_BUFFER, vertexSize +normalSize, 
					null, GL_STATIC_DRAW); // pay attention to *Float.SIZE/8
		    
			// Load the real data separately.  We put the colors right after the vertex coordinates,
		    // so, the offset for colors is the size of vertices in bytes
		    gl.glBufferSubData( GL_ARRAY_BUFFER, 0, vertexSize, vertices );
		    gl.glBufferSubData( GL_ARRAY_BUFFER, vertexSize, normalSize, normals );

			IntBuffer elements = IntBuffer.wrap(vertexIndexs);			

			long indexSize = vertexIndexs.length*(Integer.SIZE/8);
			gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexSize, 
					elements, GL_STATIC_DRAW); // pay attention to *Float.SIZE/8						
			gl.glEnableVertexAttribArray(vPosition);
			gl.glVertexAttribPointer(vPosition, 3, GL_FLOAT, false, 0, 0L);
			gl.glEnableVertexAttribArray(vNormal);
		    gl.glVertexAttribPointer(vNormal, 3, GL_FLOAT, false, 0, vertexSize);
		}

		public void bindObject(GL3 gl){
			gl.glBindVertexArray(VAOs[idPoint]);
			gl.glBindBuffer(GL_ARRAY_BUFFER, VBOs[idBuffer]);
			gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBOs[idElement]);			
		};
	}
}