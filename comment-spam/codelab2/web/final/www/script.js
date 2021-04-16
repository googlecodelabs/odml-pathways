import * as DICTIONARY from '/dictionary.js';

// The number of input elements our ML Model is expecting.
const ENCODING_LENGTH = 20;


/** 
 * Function that takes an array of words, converts words to tokens,
 * and then returns a Tensor representation of the tokenization that
 * can be used as input to the machine learning model.
 */
function tokenize(wordArray) {
  // Always start with the START token.
  let returnArray = [DICTIONARY.START];
  
  // Loop through the words in the sentence we want to encode.
  // If word is found in dictionary, add that number else
  // we add the UNKNOWN token.
  for (var i = 0; i < wordArray.length; i++) {
    let encoding = DICTIONARY.LOOKUP[wordArray[i]];
    returnArray.push(encoding === undefined ? DICTIONARY.UNKNOWN : encoding);
  }
  
  // Finally if the number of words was < the minimum encoding length
  // minus 1 (due to the start token), fill the rest with PAD tokens.
  while (i < ENCODING_LENGTH - 1) {
    returnArray.push(DICTIONARY.PAD);
    i++;
  }
  
  // Log the result to see what we made.
  console.log([returnArray]);
  
  // Convert to a TensorFlow Tensor and return that.
  return tf.tensor([returnArray]);
}


const POST_COMMENT_BTN = document.getElementById('post');
const COMMENT_TEXT = document.getElementById('comment');
const COMMENTS_LIST = document.getElementById('commentsList');
// CSS styling class to indicate comment is being processed when posting 
// to provide visual feedback to user.
const PROCESSING_CLASS = 'processing';

// Store username of logged in user. Right now we have no auth
// so default to Anonymous until known.
var currentUserName = 'Anonymous';


/** 
 * Function to handle the processing of submitted comments.
 **/
function handleCommentPost() {
  // Only continue if we are not already processing the comment.
  if (! POST_COMMENT_BTN.classList.contains(PROCESSING_CLASS)) {
    // Set styles to show processing in case it takes a long time.
    POST_COMMENT_BTN.classList.add(PROCESSING_CLASS);
    COMMENT_TEXT.classList.add(PROCESSING_CLASS);
    
    // Grab the comment text from DOM.
    let currentComment = COMMENT_TEXT.innerText;
    // Convert sentence to lower case which ML Model expects
    // Strip all characters that are not alphanumeric or spaces
    // Then split on spaces to create a word array.
    let lowercaseSentenceArray = currentComment.toLowerCase().replace(/[^\w\s]/g, ' ').split(' ');
    
    // Create a list item DOM element in memory.
    let li = document.createElement('li');
    
    // Remember loadAndPredict is asynchronous so we use the then 
    // keyword to await a result before continuing.
    loadAndPredict(tokenize(lowercaseSentenceArray), li).then(function() {
      // Reset class styles ready for the next comment.
      POST_COMMENT_BTN.classList.remove(PROCESSING_CLASS);
      COMMENT_TEXT.classList.remove(PROCESSING_CLASS);
      
      let p = document.createElement('p');
      p.innerText = COMMENT_TEXT.innerText;
      
      let spanName = document.createElement('span');
      spanName.setAttribute('class', 'username');
      spanName.innerText = currentUserName;
      
      let spanDate = document.createElement('span');
      spanDate.setAttribute('class', 'timestamp');
      let curDate = new Date();
      spanDate.innerText = curDate.toLocaleString();
      
      li.appendChild(spanName);
      li.appendChild(spanDate);
      li.appendChild(p);
      COMMENTS_LIST.prepend(li);

      // Reset comment text.
      COMMENT_TEXT.innerText = '';
    });
  }
}


POST_COMMENT_BTN.addEventListener('click', handleCommentPost);


// Set the URL below to the path of the model.json file we uploaded.
const MODEL_JSON_URL = 'model.json';
// Set the minimum confidence for spam comments to be flagged.
// Remember this is a number from 0 to 1, representing a percentage
// So here 0.75 == 75% sure it is spam.
const SPAM_THRESHOLD = 0.75;


// Create a variable to store the loaded model once it is ready so we 
// can use it elsewhere in our program later.
var model = undefined;


/** 
 * Asynchronous function to load the TFJS model and then use it to
 * predict if an input is spam or not spam. The 2nd parameter
 * allows us to specify the DOM element list item we are currently
 * classifying so we can change it's style if it is spam!
 */
async function loadAndPredict(inputTensor, domComment) {
  // Load the model.json and binary files we hosted. Note this is 
  // an asynchronous operation so we use the await keyword
  if (model === undefined) {
    model = await tf.loadLayersModel(MODEL_JSON_URL);
  }
  
  // Once model has loaded we can call model.predict and pass to it
  // an input in the form of a Tensor. We can then store the result.
  var results = await model.predict(inputTensor);
  
  // Print the result to the console for us to inspect.
  results.print();

  results.data().then((dataArray)=>{
    if (dataArray[1] > SPAM_THRESHOLD) {
      domComment.classList.add('spam');
    } else {
      // Emit socket.io comment event for server to handle containing
      // all the comment data we would need to render the comment on
      // a remote client's front end.
      socket.emit('comment', {
        username: currentUserName,
        timestamp: domComment.querySelectorAll('span')[1].innerText,
        comment: domComment.querySelectorAll('p')[0].innerText
      });
    }
  })
}


// Connect to Socket.io on our Node.js backend.
var socket = io.connect();


function handleRemoteComments(data) {
  // Render a new comment to DOM from a remote client.
  let li = document.createElement('li');
  let p = document.createElement('p');
  p.innerText = data.comment;

  let spanName = document.createElement('span');
  spanName.setAttribute('class', 'username');
  spanName.innerText = data.username;

  let spanDate = document.createElement('span');
  spanDate.setAttribute('class', 'timestamp');
  spanDate.innerText = data.timestamp;

  li.appendChild(spanName);
  li.appendChild(spanDate);
  li.appendChild(p);
  
  COMMENTS_LIST.prepend(li);
}


// Add event listener to receive remote comments that passed
// spam check.
socket.on('remoteComment', handleRemoteComments);
