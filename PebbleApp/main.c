/**
Autor: Emmanuel Arias Soto
Fecha: 07/05/2016
Version: 1.0

Esta aplicacion para el reloj Pebble alimenta a una aplicacion
para telefonos inteligentes con datos del compas, especificamente
la direccion engrados hacia donde esta apuntando el usuario

Este es el archivo main de la apliacacion, como estoy desarrollando en
CloudPebble incluyo el fuente pero para poder compilarlo debe estar ligado
a un proyecto en este IDE en linea y el archivo de imagen debe estar como
recurso con el ID IMAGE_POINT
*/
  
#include "pebble.h"

// Largest expected inbox and outbox message sizes
const uint32_t inbox_size = 64;
const uint32_t outbox_size = 256;

// Estructura de datos para enviar el mensaje a la aplicacion
typedef enum 
{
  AppKeyCompassDegree = 0
} AppKeys;

// Puntero de la ventana principal
static Window *s_main_window;

// Puntero a la capa con texto
static TextLayer *s_text_layer;

// Contenedor de la imagen y su capa
static GBitmap *s_bitmap;
static BitmapLayer *s_layer;

// Para pruebas, imprimir los grados en la capa de texto
static bool debug_mode = 0;

// Este es el manejador que se le va a pasar al servicio del compas
// Se ejecuta cada vez que el estado del compas cambia, por defecto
// se dispara cuando hay cambio de minimo 1 grado pero por ahorro se
// puede configurar a n grados.
static void compass_heading_handler(CompassHeadingData heading_data) 
{
  
  static char compass_text[10];
  static int deg;
  
  // Hay tres estados, datos invalidos o no disponibles
  // Calibrating, significa que hay que calibrar el compas y el que queda
  // es cuando el compas esta bien calibrado
  switch(heading_data.compass_status) 
  {
    case CompassStatusDataInvalid:
    if(debug_mode)
    {
      snprintf(compass_text, sizeof(compass_text), "N/A");
    }
    break;
    case CompassStatusCalibrating:
    case CompassStatusCalibrated:
    
    // arreglo del eje del servicio del compas
    // https://forums.getpebble.com/discussion/17276/compass-heading-backwards
    heading_data.magnetic_heading = TRIG_MAX_ANGLE - heading_data.magnetic_heading;
    deg = (int)TRIGANGLE_TO_DEG(heading_data.magnetic_heading) + 90;
    
    // si se pasa al sumarle los 90 grados
    if(deg >= 360)
    {
      deg %= 360;
    }
    
    if(debug_mode)
    {
      snprintf(compass_text, sizeof(compass_text), "%d°", deg);
    }
    break;
  }
  
  if(debug_mode)
  {
    text_layer_set_text(s_text_layer, compass_text);
  }
  
  
  // Declare the dictionary's iterator
  DictionaryIterator *out_iter;
  
  // Prepare the outbox buffer for this message
  AppMessageResult result = app_message_outbox_begin(&out_iter);
  
  if(result == APP_MSG_OK) {
    int value = deg;
    dict_write_int(out_iter, AppKeyCompassDegree, &value, sizeof(int), true);
    
    // Send this message
    result = app_message_outbox_send();
    if(result != APP_MSG_OK) {
    APP_LOG(APP_LOG_LEVEL_ERROR, "Error sending the outbox: %d", (int)result);
    }else{
       APP_LOG(APP_LOG_LEVEL_ERROR, "good");
    }
 
  }else{
    // The outbox cannot be used right now
    APP_LOG(APP_LOG_LEVEL_ERROR, "Error preparing the outbox: %d", (int)result);
  }
}

static void main_window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);
  
  app_message_open(inbox_size, outbox_size);
  compass_service_subscribe(compass_heading_handler);

  // We do this to account for the offset due to the status bar
  // at the top of the app window.
  GRect layer_frame_description = layer_get_frame(window_layer);
  layer_frame_description.origin.x = 0;
  layer_frame_description.origin.y = 0;

  // Add some background content to help demonstrate transparency.
  s_text_layer = text_layer_create(layer_frame_description);
  
  text_layer_set_text(s_text_layer, "Point towards\nthe device");
  layer_add_child(window_layer, text_layer_get_layer(s_text_layer));

  s_bitmap = gbitmap_create_with_resource(RESOURCE_ID_IMAGE_POINT);

  GPoint center = grect_center_point(&bounds);

  GSize image_size = gbitmap_get_bounds(s_bitmap).size;

  GRect image_frame = GRect(center.x, center.y, image_size.w, image_size.h);
  image_frame.origin.x -= image_size.w / 2;
  image_frame.origin.y -= image_size.h / 2 - 30;

  // Use GCompOpOr to display the white portions of the image
  s_layer = bitmap_layer_create(image_frame);
  bitmap_layer_set_bitmap(s_layer, s_bitmap);
  bitmap_layer_set_compositing_mode(s_layer, GCompOpSet);
  
  text_layer_set_font(s_text_layer, fonts_get_system_font(FONT_KEY_ROBOTO_CONDENSED_21));
	text_layer_set_text_alignment(s_text_layer, GTextAlignmentCenter);
  layer_add_child(window_layer, bitmap_layer_get_layer(s_layer));
  
  
}

static void main_window_unload(Window *window) {
  bitmap_layer_destroy(s_layer);
  text_layer_destroy(s_text_layer);
  gbitmap_destroy(s_bitmap);
  
  // eliminar la suscripcion del servicio del compas
  compass_service_unsubscribe();
}

static void init(void) {
  s_main_window = window_create();
  window_set_window_handlers(s_main_window, (WindowHandlers) {
    .load = main_window_load,
    .unload = main_window_unload
  });
  window_stack_push(s_main_window, true);
}

static void deinit(void) {
  window_destroy(s_main_window);
}

int main(void) {
  init();
  app_event_loop();
  deinit();
}
