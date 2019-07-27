import React from 'react';
import { withStyles } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import CircularProgress from '@material-ui/core/CircularProgress';
import axios from 'axios'
import { Container, Card, CardHeader, CardContent, CardActions,
   IconButton, Tooltip, LinearProgress } from '@material-ui/core';
import FavoriteIcon from '@material-ui/icons/Favorite';

import NewPostDialogst from "./NewPostDialog"
import NewPostDialog from './NewPostDialog';

const styles = theme => ({
  root: {
    flexGrow: 1,
  },
  menuButton: {
    marginRight: theme.spacing(2),
  },
  title: {
    flexGrow: 1,
  },
});

export default withStyles(styles) (

  class App extends React.Component {

    constructor(props) {
      super(props)

      this.state = {
        currentUser: "loading",
        posts: "loading",
        dialogOpen: false
      }
    }

    reloadPosts() {
      axios.get("/api/posts")
      .then(function(response){
        console.log(response)
        this.setState({
          posts: response.data
        })
      }.bind(this))
    }

    componentDidMount() {
      axios.get("/api/current-user")
      .then(function(response){
        console.log(response)
        this.setState({
          currentUser: response.data
        })
      }.bind(this))

      this.reloadPosts.bind(this)()
    }

    goSignIn() {
      window.location.replace("/signin");
    }

    goSignUp() {
      window.location.replace("/signup");
    }

    goSignOut() {
      axios.get("/api/signout")
      .then(function(response){
        console.log(response)
        this.setState({
          currentUser: {}
        })
      }.bind(this))
    }

    showNewPostDialog() {
      this.setState({dialogOpen: true})
    }

    newPostDialogOnClose(resPost) {
      console.log("dialog closed:")
      console.log(resPost)
      this.setState({ dialogOpen: false })

      if(resPost) {
        axios.post('/api/posts', resPost)
        .then(function (response) {
          let rs = response.status;
          if(rs == 200) {
            console.log(response)
            this.reloadPosts.bind(this)()
          } else {
            this.setState({inProgress:false})
            alert("Something went wrong...")
          }
        }.bind(this))
        .catch(function (error) {
          console.log(error);
          this.setState({inProgress:false})
          alert("Something went wrong...")
        }.bind(this));
      }
    }

    likePost(postId) {
      axios.post('/api/posts/like', {postID: postId})
        .then(function (response) {
          let rs = response.status;
          if(rs == 200) {
            console.log(response)
            this.reloadPosts.bind(this)()
          } else {
            this.setState({inProgress:false})
            alert("Something went wrong...")
          }
        }.bind(this))
        .catch(function (error) {
          console.log(error);
          this.setState({inProgress:false})
          alert("Something went wrong...")
        }.bind(this));
    }

    render() {
      const { classes } = this.props;

      let userSpace
      if (this.state.currentUser == "loading") {
        userSpace = <CircularProgress />
      } else if (this.state.currentUser.username) {
        userSpace = 
        <Toolbar>
          <Typography variant="h6" className={classes.title}>
            {this.state.currentUser.username}
          </Typography>
          <Button onClick={this.goSignOut.bind(this)} color="inherit">SignOut</Button>
        </Toolbar>
      } else {
        userSpace = 
        <Toolbar>
          <Button onClick={this.goSignIn} color="inherit">SignIn</Button>
          <Button onClick={this.goSignUp} color="inherit">SignUp</Button>
        </Toolbar>
      }

      return (
        <div className={classes.root}>
          <AppBar position="static">
            <Toolbar>
              
              <Typography variant="h4" className={classes.title}>
                wallblog
              </Typography>
              { userSpace }
            </Toolbar>
          </AppBar>
          <Container>
            <NewPostDialog open={this.state.dialogOpen} onClose={this.newPostDialogOnClose.bind(this)} />
            {(this.state.currentUser.username)?<Button onClick = {this.showNewPostDialog.bind(this)}> + Add Post </Button>:""}

            { (this.state.posts == "loading")? <LinearProgress /> : 
              <div>
              {
                this.state.posts.map((v, i) => {
                  return (
                    <Card key={v.id}>
                    <CardHeader title={ v.title } subheader={"by " + v.author.firstName + " " + v.author.lastName} />
                      
                    <CardContent>
                      <Typography variant="body2" color="textSecondary" component="p">
                        { v.content }
                      </Typography>
                    </CardContent>
                    <CardActions disableSpacing>
                        <IconButton onClick={_ => this.likePost.bind(this)(v.id)} aria-label="add to favorites">
                          <FavoriteIcon />
                        </IconButton>
                        <Tooltip title={v.likes.map(l => l.firstName + " " + l.lastName).join(", ")} aria-label="add">
                        <Typography> 
                          {v.likes.length}
                        </Typography>
                        </Tooltip>
                    </CardActions>
                  </Card>
                )})}
                </div>
            }
          </Container>
        </div>
      );
    }
  }
)