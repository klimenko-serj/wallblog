import React from 'react';
import PropTypes from 'prop-types';
import { makeStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import Avatar from '@material-ui/core/Avatar';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemAvatar from '@material-ui/core/ListItemAvatar';
import ListItemText from '@material-ui/core/ListItemText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import TextField from '@material-ui/core/TextField';
import PersonIcon from '@material-ui/icons/Person';
import AddIcon from '@material-ui/icons/Add';
import Typography from '@material-ui/core/Typography';
import { blue } from '@material-ui/core/colors';

const useStyles = makeStyles({
  avatar: {
    backgroundColor: blue[100],
    color: blue[600],
  },
});

export default function NewPostDialog(props) {
  const classes = useStyles();
  const { onClose, open } = props;

  function handleClose() {
    onClose();
  }
  
  function handlePost() {
    let t = document.getElementById("title").value
    let c  = document.getElementById("content").value
    
    if(!(t && c)){
      alert("Fill all fields!")
      return;
    }

    onClose({
        title: t,
        content: c
    });
  }


  return (
    <Dialog onClose={handleClose} aria-labelledby="simple-dialog-title" open={open}>
      <DialogTitle id="simple-dialog-title">New Blog Post</DialogTitle>
      <DialogContent>
        <TextField
            autoFocus
            margin="dense"
            id="title"
            label="Title"
            fullWidth
        />
        <TextField
            id="content"
            label="Content"
            margin="normal"
            variant="outlined"
            multiline
            fullWidth
        // rowsMax="8"
        />
      </DialogContent>
      <DialogActions>
          <Button onClick={handleClose} color="primary">
            Cancel
          </Button>
          <Button onClick={handlePost} color="primary">
            Post
          </Button>
        </DialogActions>
    </Dialog>
  );
}

NewPostDialog.propTypes = {
  onClose: PropTypes.func.isRequired,
  open: PropTypes.bool.isRequired,
};
