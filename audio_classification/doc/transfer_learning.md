# Customizing an Audio Classification model

## Pre-trained models

Machine learning models are computational components specialized in solving a specific problem in a specific domain. For example, a model created to do Audio Classification, like [YAMNet](https://tfhub.dev/google/yamnet/1), is created with a set of classes that it learns how to classify (e.g.: dogs, cats, finger snapping, clapping, etc.). These models are also called pre-trained models and are usually trained on very large datasets. You can find a large collections of pre-trained models on [TensorFlow Hub](http://tfhu.dev).

But what if the problem you are trying to solve is more specific and there is no pre-trained model that can help you?

For example, imagine you want to classify bird songs. YAMNet has a bird class, but it does not have many species of birds because it's a more generic model. What should you do in this situation?

This is a very common problem in Machine Learning. This is such a common problem that there's a common solution: Transfer Learning.

## What is Transfer Learning?
The idea behind transfer learning is simple: instead of learning the weights and biases in a neural network from scratch, why not use a set of filters that were already learned by a different model that was trained on a much larger dataset? You can use a smaller dataset, but the features that identify sounds could be the same as those in a much larger, more general dataset. 

So, if there’s an existing model that learned them, we can take advantage of that, by using the features it learned in our network. For example, the YAMNet, which is a very common audio classification model, can predict 521 classes, having been trained on over 2 million audio events – so what if we take the features that this model learned, but instead of outputting 521 potential outputs, we simply replace its classification head with one that recognizes the 5 birds we want to recognize, simplifying the model greatly, and using what YAMNet has already learned about identifying features in audio, instead of reinventing the wheel and training it ourselves from scratch. 

This shows what the architecture for YAMNet might look like. It’s comprised of a number of convolutional layers, followed by dense layers which perform the classification to 521 outputs.

![YAMNet Architecture](/audio_classification/doc/transfer_learning_fig_1.png)

With transfer learning, we could take the pre-learned layers from YAMNet, freeze them so that they aren’t trainable, and then add our desired classification head, with only 5 output neurons in it, like this:

![YAMNet Architecture](/audio_classification/doc/transfer_learning_fig_2.png)

When we consider that, as they have already been trained, all these layers are just sets of numbers indicating filter values, weights, and biases, along with a known architecture (number of filters per layer, size of filter, etc.), the idea of reusing them is pretty straightforward.

## Specific challenges for the audio domain

Just like images have resolution and color depth, audio will also have some specific characteristics, like sample rate, number of channels, and bit depth, that can impact the quality of your model.

When you customize a model, the base model has a predetermined input specification. Taking YAMNet as an example, the audio should be mono channel with a 16 kHz sample rate. This means that if you have audio files recorded with a higher sampling rate, you will need to downsample them. 

Unfortunately not all audio can be downsampled because, depending on the original frequency of what was recorded, downsampling could hide the original audio frequency in an unrecoverable way. You can learn all the physics reasons for it here: [Nyquist–Shannon sampling theorem](https://en.wikipedia.org/wiki/Nyquist%E2%80%93Shannon_sampling_theorem). 

One example of this is some birds sing at a very high frequency (e.g.: the [Bananaquit](https://www.xeno-canto.org/species/Coereba-flaveola?view=3)). These birds cannot be recorded (or downsampled to) a 16kHz sample rate. It would represent the wrong frequency and your machine learning model wouldn't be able to find the correct pattern in the data.

## Capturing your own data

Usually any audio recording can be converted to a format that is used with Machine Learning, but if you can record in the WAV format that will make your life easier later.

The audio recorder configuration should be set to what your base model expects. In the YAMNet case, a mono channel with 16 kHz sample rate and 16 bitrate depth.

When recording audio for one specific class, it's important to have a recording with different background noises. The closer to a real situation the better the model will perform when using it later in the real world.

One technique that  you can use is to have an extra class on your dataset that is background noise. This can help make your model's results more precise.

## Splitting your own data

When training Machine Learning models, you will usually need to split your dataset into, at least, a training and a test set ([why?](https://developers.google.com/machine-learning/crash-course/training-and-test-sets/splitting-data)). With the audio domain, you will have multiple recordings and you can cut from a long audio into multiple short ones. That makes data augmentation easier.

An important thing to remember is that if you cut one audio in multiple files, and you place one of these files in one specific split, all the others must also be on the same split. Otherwise you'd be leaking data and that would make the results of your model less reliable.

## Next steps

Now that you have a better understanding of how to do transfer learning for the audio domain, you can try with real data and deploy on a mobile app.

The following codelab in this pathway:[Build a custom pre-trained Audio Classification model](https://codelabs.developers.google.com/codelabs/tflite-audio-classification-custom-model-android), will guide you through the whole process using a bird song dataset.
